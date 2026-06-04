package com.industrial.productionsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private DashboardRequest request;

    // ── Contadores gerais ────────────────────────────────────────────
    private long totalMaquinas;
    private long maquinasAtivas;
    private long maquinasInativas;
    private long maquinasEmManutencao;

    private long totalProdutos;

    private long totalOrdens;
    private long ordensPendentes;
    private long ordensEmProducao;
    private long ordensFinalizada;

    // ── Produção ─────────────────────────────────────────────────────
    /** Soma de quantidade das ordens FINALIZADAS */
    private long totalUnidadesProduzidas;

    /** Soma de quantidade das ordens PENDENTES + EM_PRODUCAO */
    private long totalUnidadesEmAberto;

    // ── Ranking de máquinas ──────────────────────────────────────────
    /** Top 5 máquinas por número de ordens finalizadas */
    private List<MaquinaRankingItem> topMaquinas;

    @Data
    @Builder
    public static class MaquinaRankingItem {
        private Long maquinaId;
        private String maquinaNome;
        private long ordensFinalizadas;
        private long unidadesProduzidas;
    }
}