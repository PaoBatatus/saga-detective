package com.example.demo.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.event.RecursoCriadoEvent;
import com.example.demo.model.Suspeito;
import com.example.demo.repository.SuspeitoRepository;
import com.example.demo.service.SuspeitoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/suspeitos")
public class SuspeitoResource {

    @Autowired
    private SuspeitoRepository suspeitoRepository;

    @Autowired
    private SuspeitoService suspeitoService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_SUSPEITO') and hasAuthority('SCOPE_read')")
    public List<Suspeito> listar(@RequestParam(required = false) Long casoId) {
        if (casoId != null) {
            return suspeitoRepository.findByCasoId(casoId);
        }
        return suspeitoRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_SUSPEITO') and hasAuthority('SCOPE_read')")
    public ResponseEntity<Suspeito> buscarPorId(@PathVariable Long id) {
        return suspeitoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_SUSPEITO') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Suspeito> criar(@Valid @RequestBody Suspeito suspeito, HttpServletResponse response) {
        suspeito.setId(null);
        Suspeito salvo = suspeitoRepository.save(suspeito);

        publisher.publishEvent(new RecursoCriadoEvent(this, response, salvo.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_SUSPEITO') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Suspeito> atualizar(@PathVariable Long id, @Valid @RequestBody Suspeito suspeito) {
        Suspeito suspeitoSalvo = suspeitoService.atualizar(id, suspeito);
        return ResponseEntity.ok(suspeitoSalvo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_REMOVER_SUSPEITO') and hasAuthority('SCOPE_write')")
    public void remover(@PathVariable Long id) {
        suspeitoService.remover(id);
    }
}