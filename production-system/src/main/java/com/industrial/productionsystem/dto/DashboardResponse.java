package com.industrial.productionsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ── Máquinas ──────────────────────────────────────────────────
    private long totalMaquinas;
    private long maquinasAtivas;
    private long maquinasInativas;
    private long maquinasEmManutencao;

    // ── Produtos ──────────────────────────────────────────────────
    private long totalProdutos;

    // ── Ordens ────────────────────────────────────────────────────
    private long totalOrdens;
    private long ordensPendentes;
    private long ordensEmProducao;
    private long ordensFinalizada;

    private long totalUnidadesProduzidas;
    private long totalUnidadesEmAberto;

    // ── Ranking de máquinas (top 5 por ordens finalizadas) ────────
    private List<MaquinaRankingItem> topMaquinas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaquinaRankingItem {
        private Long maquinaId;
        private String maquinaNome;
        private long ordensFinalizadas;
        private long unidadesProduzidas;
    }
}