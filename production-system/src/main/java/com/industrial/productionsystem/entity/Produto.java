package com.industrial.production_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer tempoProducaoUnitario;

    // getters e setters
    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getTempoProducaoUnitario() { return tempoProducaoUnitario; }
    public void setTempoProducaoUnitario(Integer tempo) {
        this.tempoProducaoUnitario = tempo;
    }
}