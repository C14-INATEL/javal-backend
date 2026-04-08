package com.example.javalbackend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "maquinas")
public class Maquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMaquina status;

    @Column(nullable = false)
    private Integer capacidadePorHora;

    // Construtor vazio (JPA)
    public Maquina() {
    }

    // Construtor útil
    public Maquina(String nome, String tipo, Integer capacidadePorHora) {
        this.nome = nome;
        this.tipo = tipo;
        this.capacidadePorHora = capacidadePorHora;
        this.status = StatusMaquina.ATIVA; // padrão
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public StatusMaquina getStatus() {
        return status;
    }

    public void setStatus(StatusMaquina status) {
        this.status = status;
    }

    public Integer getCapacidadePorHora() {
        return capacidadePorHora;
    }

    public void setCapacidadePorHora(Integer capacidadePorHora) {
        this.capacidadePorHora = capacidadePorHora;
    }
}