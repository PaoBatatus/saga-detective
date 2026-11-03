package com.example.demo.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Suspeito;
import com.example.demo.repository.SuspeitoRepository;

@Service
public class SuspeitoService {

    @Autowired
    private SuspeitoRepository suspeitoRepository;

    public Suspeito atualizar(Long id, Suspeito suspeito) {
        Suspeito suspeitoSalvo = buscarPeloId(id);
        BeanUtils.copyProperties(suspeito, suspeitoSalvo, "id");
        return suspeitoRepository.save(suspeitoSalvo);
    }

    public void remover(Long id) {
        Suspeito suspeitoSalvo = buscarPeloId(id);
        suspeitoRepository.delete(suspeitoSalvo);
    }

    private Suspeito buscarPeloId(Long id) {
        return suspeitoRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }
}
