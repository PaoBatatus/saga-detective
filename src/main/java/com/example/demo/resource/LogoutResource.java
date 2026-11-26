package com.example.demo.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Profile("oauth-security")
@RestController
@RequestMapping("/tokens")
public class LogoutResource {

    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    @Value("${app.security.https-enabled:false}")
    private boolean httpsHabilitado;

    @DeleteMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revogar(HttpServletRequest request, HttpServletResponse response) {
        // Mesmos atributos do cookie criado no /oauth/token, com validade zero
        ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(httpsHabilitado)
                .path(request.getContextPath() + "/oauth/token")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
