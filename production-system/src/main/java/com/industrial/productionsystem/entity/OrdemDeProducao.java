package com.industrial.production_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_producao")
public class OrdemDeProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private StatusOrdem status;

    private String maquinaResponsavel;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    // getters e setters
}