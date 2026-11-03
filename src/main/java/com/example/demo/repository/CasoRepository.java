package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Caso;
import com.example.demo.repository.caso.CasoRepositoryQuery;

public interface CasoRepository extends JpaRepository<Caso, Long>, CasoRepositoryQuery {

}