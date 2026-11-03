package com.example.demo.repository.filter;

public class CasoFilter {

    private String titulo;

    private String dificuldade;

    private Integer limiteHorasDe;

    private Integer limiteHorasAte;

    private Boolean ativo = true;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public Integer getLimiteHorasDe() {
        return limiteHorasDe;
    }

    public void setLimiteHorasDe(Integer limiteHorasDe) {
        this.limiteHorasDe = limiteHorasDe;
    }

    public Integer getLimiteHorasAte() {
        return limiteHorasAte;
    }

    public void setLimiteHorasAte(Integer limiteHorasAte) {
        this.limiteHorasAte = limiteHorasAte;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}