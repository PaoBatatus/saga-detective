package com.example.demo.repository.caso;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.example.demo.model.Caso;
import com.example.demo.repository.filter.CasoFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class CasoRepositoryImpl implements CasoRepositoryQuery {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Page<Caso> filtrar(CasoFilter casoFilter, Pageable pageable) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Caso> criteria = builder.createQuery(Caso.class);
        Root<Caso> root = criteria.from(Caso.class);

        Predicate[] predicates = criarRestricoes(casoFilter, builder, root);
        criteria.where(predicates);
        criteria.orderBy(builder.asc(root.get("id")));

        TypedQuery<Caso> query = manager.createQuery(criteria);
        adicionarRestricoesPaginacao(query, pageable);

        return new PageImpl<>(query.getResultList(), pageable, total(casoFilter));
    }

    private Long total(CasoFilter casoFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Caso> root = criteria.from(Caso.class);

        Predicate[] predicates = criarRestricoes(casoFilter, builder, root);
        criteria.where(predicates);

        criteria.select(builder.count(root));
        return manager.createQuery(criteria).getSingleResult();
    }

    private void adicionarRestricoesPaginacao(TypedQuery<Caso> query, Pageable pageable) {
        int paginaAtual = pageable.getPageNumber();
        int totalRegistrosPorPagina = pageable.getPageSize();
        int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;

        query.setFirstResult(primeiroRegistroDaPagina);
        query.setMaxResults(totalRegistrosPorPagina);
    }

    private Predicate[] criarRestricoes(CasoFilter casoFilter, CriteriaBuilder builder, Root<Caso> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(casoFilter.getTitulo())) {
            predicates.add(builder.like(
                    builder.lower(root.get("titulo")),
                    "%" + casoFilter.getTitulo().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(casoFilter.getDificuldade())) {
            predicates.add(builder.equal(
                    root.get("dificuldade"),
                    casoFilter.getDificuldade().toUpperCase()));
        }

        if (casoFilter.getLimiteHorasDe() != null) {
            predicates.add(builder.greaterThanOrEqualTo(
                    root.<Integer>get("limiteHoras"),
                    casoFilter.getLimiteHorasDe()));
        }

        if (casoFilter.getLimiteHorasAte() != null) {
            predicates.add(builder.lessThanOrEqualTo(
                    root.<Integer>get("limiteHoras"),
                    casoFilter.getLimiteHorasAte()));
        }

        if (casoFilter.getAtivo() != null) {
            predicates.add(builder.equal(root.get("ativo"), casoFilter.getAtivo()));
        }

        return predicates.toArray(new Predicate[0]);
    }
}