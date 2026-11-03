package com.example.demo.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Caso;
import com.example.demo.repository.CasoRepository;

@Service
public class CasoService {

    @Autowired
    private CasoRepository casoRepository;

    public Caso atualizar(Long id, Caso caso) {
        Caso casoSalvo = buscarPeloId(id);
        BeanUtils.copyProperties(caso, casoSalvo, "id", "ativo");
        return casoRepository.save(casoSalvo);
    }

    public void atualizarPropriedadeAtivo(Long id, Boolean ativo) {
        Caso casoSalvo = buscarPeloId(id);
        casoSalvo.setAtivo(ativo);
        casoRepository.save(casoSalvo);
    }

    public void desativar(Long id) {
        atualizarPropriedadeAtivo(id, false);
    }

    private Caso buscarPeloId(Long id) {
        return casoRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }
}