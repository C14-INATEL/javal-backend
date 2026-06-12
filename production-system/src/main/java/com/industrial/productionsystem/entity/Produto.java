package com.industrial.productionsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer tempoProducaoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public Produto() {}

    public Produto(String nome, Integer tempoProducaoUnitario, Company company) {
        this.nome = nome;
        this.tempoProducaoUnitario = tempoProducaoUnitario;
        this.company = company;
    }
}