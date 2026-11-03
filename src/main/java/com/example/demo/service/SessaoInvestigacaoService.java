package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Caso;
import com.example.demo.model.SessaoInvestigacao;
import com.example.demo.repository.CasoRepository;
import com.example.demo.repository.SessaoInvestigacaoRepository;
import com.example.demo.service.exception.CasoInexistenteOuInativoException;

@Service
public class SessaoInvestigacaoService {

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private SessaoInvestigacaoRepository sessaoRepository;

    public SessaoInvestigacao salvar(SessaoInvestigacao sessao) {
        Long casoId = sessao.getCaso().getId();
        Caso caso = casoId == null ? null : casoRepository.findById(casoId).orElse(null);

        if (caso == null || caso.isInativo()) {
            throw new CasoInexistenteOuInativoException();
        }

        sessao.setId(null);
        sessao.setCaso(caso);
        return sessaoRepository.save(sessao);
    }
}