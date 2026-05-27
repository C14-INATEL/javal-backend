package com.industrial.productionsystem.entity;

import com.industrial.productionsystem.entity.enums.StatusMaquina;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public Maquina() {}

    public Maquina(String nome, String tipo, Integer capacidadePorHora, Company company) {
        this.nome = nome;
        this.tipo = tipo;
        this.capacidadePorHora = capacidadePorHora;
        this.status = StatusMaquina.ATIVA;
        this.company = company;
    }

    public Maquina(String string, String string2, int i) {
        //TODO Auto-generated constructor stub
    }
}