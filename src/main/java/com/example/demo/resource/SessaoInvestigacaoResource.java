package com.example.demo.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.event.RecursoCriadoEvent;
import com.example.demo.model.SessaoInvestigacao;
import com.example.demo.repository.SessaoInvestigacaoRepository;
import com.example.demo.service.SessaoInvestigacaoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/sessoes")
public class SessaoInvestigacaoResource {

    @Autowired
    private SessaoInvestigacaoRepository sessaoRepository;

    @Autowired
    private SessaoInvestigacaoService sessaoService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_SESSAO') and hasAuthority('SCOPE_read')")
    public List<SessaoInvestigacao> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null) {
            return sessaoRepository.findByUsuarioId(usuarioId);
        }
        return sessaoRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_SESSAO') and hasAuthority('SCOPE_read')")
    public ResponseEntity<SessaoInvestigacao> buscarPorId(@PathVariable Long id) {
        return sessaoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_INICIAR_SESSAO') and hasAuthority('SCOPE_write')")
    public ResponseEntity<SessaoInvestigacao> criar(@Valid @RequestBody SessaoInvestigacao sessao,
            HttpServletResponse response) {
        SessaoInvestigacao salva = sessaoService.salvar(sessao);

        publisher.publishEvent(new RecursoCriadoEvent(this, response, salva.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }
}