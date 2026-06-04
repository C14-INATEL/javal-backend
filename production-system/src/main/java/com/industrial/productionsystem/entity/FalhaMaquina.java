package com.industrial.productionsystem.entity;

import com.industrial.productionsystem.entity.enums.SeveridadeFalha;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "falhas_maquina")
public class FalhaMaquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadeFalha severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFalha status;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataResolucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public FalhaMaquina() {}

    @PrePersist
    public void prePersist() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusFalha.ABERTA;
        }
    }
}