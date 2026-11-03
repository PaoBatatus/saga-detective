package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Suspeito;

public interface SuspeitoRepository extends JpaRepository<Suspeito, Long> {

    List<Suspeito> findByCasoId(Long casoId);
}