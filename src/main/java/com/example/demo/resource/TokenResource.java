package com.example.demo.resource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.ClienteOAuthProperties;
import com.example.demo.config.ClienteOAuthProperties.Cliente;
import com.example.demo.exceptionhandler.SagaExceptionHandler.Erro;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Profile("oauth-security")
@RestController
@RequestMapping("/oauth")
public class TokenResource {

    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    public record TokenResponse(String access_token, String token_type, long expires_in, String scope,
            String jti) {
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    @Qualifier("refreshTokenDecoder")
    private JwtDecoder refreshTokenDecoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ClienteOAuthProperties clientesProperties;

    @Value("${app.jwt.expiration-seconds:900}")
    private long accessTokenSegundos;

    @Value("${app.jwt.refresh-expiration-seconds:86400}")
    private long refreshTokenSegundos;

    @Value("${app.security.https-enabled:false}")
    private boolean httpsHabilitado;

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> gerarToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @CookieValue(name = COOKIE_REFRESH_TOKEN, required = false) String refreshTokenDoCookie,
            HttpServletRequest request, HttpServletResponse response) {

        Optional<Cliente> cliente = autenticarCliente(authorization);
        if (cliente.isEmpty()) {
            return erro(HttpStatus.UNAUTHORIZED, "Cliente não autorizado",
                    "Credenciais do cliente ausentes ou inválidas");
        }

        return switch (grantType) {
            case "password" -> tokenPorSenha(cliente.get(), username, password, request, response);
            case "refresh_token" -> tokenPorRefreshToken(cliente.get(), refreshTokenDoCookie, request, response);
            default -> erro(HttpStatus.BAD_REQUEST, "Tipo de autorização não suportado",
                    "Use grant_type=password ou grant_type=refresh_token");
        };
    }

    private ResponseEntity<?> tokenPorSenha(Cliente cliente, String username, String password,
            HttpServletRequest request, HttpServletResponse response) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return erro(HttpStatus.BAD_REQUEST, "Informe usuário e senha",
                    "Os parâmetros username e password são obrigatórios");
        }

        Authentication autenticacao;
        try {
            autenticacao = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            return erro(HttpStatus.BAD_REQUEST, "Usuário ou senha inválidos", ex.toString());
        }

        List<String> papeis = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(emitirTokens(cliente, autenticacao.getName(), papeis, request, response));
    }

    private ResponseEntity<?> tokenPorRefreshToken(Cliente cliente, String refreshToken,
            HttpServletRequest request, HttpServletResponse response) {
        if (!StringUtils.hasText(refreshToken)) {
            return erro(HttpStatus.BAD_REQUEST, "Informe o refresh token",
                    "O cookie refreshToken nao foi enviado");
        }

        Jwt jwt;
        try {
            jwt = refreshTokenDecoder.decode(refreshToken);
        } catch (JwtException ex) {
            return erro(HttpStatus.BAD_REQUEST, "Refresh token inválido ou expirado", ex.getMessage());
        }

        // O refresh token so vale para o cliente que o recebeu
        if (!cliente.getId().equals(jwt.getClaimAsString("client_id"))) {
            return erro(HttpStatus.BAD_REQUEST, "Refresh token inválido ou expirado",
                    "O refresh token pertence a outro cliente");
        }

        // Recarrega o usuario: se ele foi removido ou desativado, o token deixa de servir
        UserDetails usuario;
        try {
            usuario = userDetailsService.loadUserByUsername(jwt.getSubject());
        } catch (UsernameNotFoundException ex) {
            return erro(HttpStatus.BAD_REQUEST, "Refresh token inválido ou expirado", ex.toString());
        }

        if (!usuario.isEnabled()) {
            return erro(HttpStatus.BAD_REQUEST, "Refresh token inválido ou expirado", "Usuário desativado");
        }

        List<String> papeis = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(emitirTokens(cliente, usuario.getUsername(), papeis, request, response));
    }

    private TokenResponse emitirTokens(Cliente cliente, String usuario, List<String> papeis,
            HttpServletRequest request, HttpServletResponse response) {
        Instant agora = Instant.now();
        String jtiAccess = UUID.randomUUID().toString();
        List<String> escopos = cliente.getScopes();

        JwtClaimsSet claimsAccess = JwtClaimsSet.builder()
                .id(jtiAccess)
                .subject(usuario)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(accessTokenSegundos))
                .claim("client_id", cliente.getId())
                .claim("scope", escopos)
                .claim("authorities", papeis)
                .claim("token_use", "access")
                .build();

        JwtClaimsSet claimsRefresh = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(refreshTokenSegundos))
                .claim("client_id", cliente.getId())
                .claim("token_use", "refresh")
                .build();

        // O refresh token vai so no cookie HttpOnly, nunca no corpo da resposta
        adicionarRefreshTokenNoCookie(assinar(claimsRefresh), request, response);

        return new TokenResponse(assinar(claimsAccess), "bearer", accessTokenSegundos,
                String.join(" ", escopos), jtiAccess);
    }

    private void adicionarRefreshTokenNoCookie(String refreshToken, HttpServletRequest request,
            HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(httpsHabilitado)
                .path(request.getContextPath() + "/oauth/token")
                .maxAge(refreshTokenSegundos)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String assinar(JwtClaimsSet claims) {
        return jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private Optional<Cliente> autenticarCliente(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return Optional.empty();
        }

        String recebido;
        try {
            recebido = new String(Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        int separador = recebido.indexOf(':');
        if (separador < 0) {
            return Optional.empty();
        }

        String id = recebido.substring(0, separador);
        String segredo = recebido.substring(separador + 1);

        return clientesProperties.getClients().stream()
                .filter(c -> c.getId().equals(id))
                .filter(c -> MessageDigest.isEqual(
                        segredo.getBytes(StandardCharsets.UTF_8),
                        c.getSecret().getBytes(StandardCharsets.UTF_8)))
                .findFirst();
    }

    private ResponseEntity<Object> erro(HttpStatus status, String mensagemUsuario, String mensagemDesenvolvedor) {
        return ResponseEntity.status(status).body(List.of(new Erro(mensagemUsuario, mensagemDesenvolvedor)));
    }
}