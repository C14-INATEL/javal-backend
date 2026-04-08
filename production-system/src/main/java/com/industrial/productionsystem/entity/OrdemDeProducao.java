package com.industrial.productionsystem.entity;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ordens_producao")
@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    // getters e setters
}