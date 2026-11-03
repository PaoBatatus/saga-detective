package com.example.demo.repository.caso;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.model.Caso;
import com.example.demo.repository.filter.CasoFilter;

public interface CasoRepositoryQuery {

    Page<Caso> filtrar(CasoFilter casoFilter, Pageable pageable);
}