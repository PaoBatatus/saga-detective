package com.example.demo.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "caso")
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 150)
    private String titulo;

    @NotBlank
    @Column(name = "descricao_crime")
    private String descricaoCrime;

    @NotBlank
    @Pattern(regexp = "FACIL|MEDIO|DIFICIL")
    private String dificuldade;

    @NotNull
    @Positive
    @Column(name = "limite_horas")
    private Integer limiteHoras;

    @Column(name = "imagem_capa")
    private String imagemCapa;

    private Boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricaoCrime() {
        return descricaoCrime;
    }

    public void setDescricaoCrime(String descricaoCrime) {
        this.descricaoCrime = descricaoCrime;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public Integer getLimiteHoras() {
        return limiteHoras;
    }

    public void setLimiteHoras(Integer limiteHoras) {
        this.limiteHoras = limiteHoras;
    }

    public String getImagemCapa() {
        return imagemCapa;
    }

    public void setImagemCapa(String imagemCapa) {
        this.imagemCapa = imagemCapa;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @JsonIgnore
    @Transient
    public boolean isInativo() {
        return !Boolean.TRUE.equals(this.ativo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Caso outro = (Caso) obj;
        return Objects.equals(id, outro.id);
    }
}