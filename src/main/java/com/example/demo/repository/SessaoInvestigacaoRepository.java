package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SessaoInvestigacao;

public interface SessaoInvestigacaoRepository extends JpaRepository<SessaoInvestigacao, Long> {

    List<SessaoInvestigacao> findByUsuarioId(Long usuarioId);
}