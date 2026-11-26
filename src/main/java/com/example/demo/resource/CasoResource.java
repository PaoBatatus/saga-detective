package com.example.demo.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.event.RecursoCriadoEvent;
import com.example.demo.model.Caso;
import com.example.demo.repository.CasoRepository;
import com.example.demo.repository.filter.CasoFilter;
import com.example.demo.service.CasoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/casos")
public class CasoResource {

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private CasoService casoService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_CASO') and hasAuthority('SCOPE_read')")
    public Page<Caso> pesquisar(CasoFilter casoFilter, Pageable pageable) {
        return casoRepository.filtrar(casoFilter, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_CASO') and hasAuthority('SCOPE_read')")
    public ResponseEntity<Caso> buscarPorId(@PathVariable Long id) {
        return casoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_CASO') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Caso> criar(@Valid @RequestBody Caso caso, HttpServletResponse response) {
        caso.setId(null);
        Caso salvo = casoRepository.save(caso);

        publisher.publishEvent(new RecursoCriadoEvent(this, response, salvo.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_CASO') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Caso> atualizar(@PathVariable Long id, @Valid @RequestBody Caso caso) {
        Caso casoSalvo = casoService.atualizar(id, caso);
        return ResponseEntity.ok(casoSalvo);
    }

    @PutMapping("/{id}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_CASO') and hasAuthority('SCOPE_write')")
    public void atualizarPropriedadeAtivo(@PathVariable Long id, @RequestBody Boolean ativo) {
        casoService.atualizarPropriedadeAtivo(id, ativo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_REMOVER_CASO') and hasAuthority('SCOPE_write')")
    public void desativar(@PathVariable Long id) {
        casoService.desativar(id);
    }
}