package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.DashboardResponse;
import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.MaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import com.industrial.productionsystem.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MaquinaRepository maquinaRepository;
    private final ProdutoRepository produtoRepository;
    private final OrdemDeProducaoRepository ordemRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long companyId) {

        // ── Máquinas ─────────────────────────────────────────────────
        var maquinas = maquinaRepository.findByCompanyId(companyId);
        long totalMaquinas      = maquinas.size();
        long maquinasAtivas     = maquinas.stream().filter(m -> m.getStatus() == StatusMaquina.ATIVA).count();
        long maquinasInativas   = maquinas.stream().filter(m -> m.getStatus() == StatusMaquina.INATIVA).count();
        long maquinasManutencao = maquinas.stream().filter(m -> m.getStatus() == StatusMaquina.MANUTENCAO).count();

        // ── Produtos ─────────────────────────────────────────────────
        long totalProdutos = produtoRepository.findByCompanyId(companyId).size();

        // ── Ordens ───────────────────────────────────────────────────
        var ordens = ordemRepository.findByCompanyId(companyId);

        long totalOrdens       = ordens.size();
        long ordensPendentes   = ordens.stream().filter(o -> o.getStatus() == StatusOrdem.PENDENTE).count();
        long ordensEmProducao  = ordens.stream().filter(o -> o.getStatus() == StatusOrdem.EM_PRODUCAO).count();
        long ordensFinalizada  = ordens.stream().filter(o -> o.getStatus() == StatusOrdem.FINALIZADA).count();

        long totalUnidadesProduzidas = ordens.stream()
                .filter(o -> o.getStatus() == StatusOrdem.FINALIZADA)
                .mapToLong(OrdemDeProducao::getQuantidade)
                .sum();

        long totalUnidadesEmAberto = ordens.stream()
                .filter(o -> o.getStatus() == StatusOrdem.PENDENTE
                        || o.getStatus() == StatusOrdem.EM_PRODUCAO)
                .mapToLong(OrdemDeProducao::getQuantidade)
                .sum();

        // ── Ranking de máquinas ───────────────────────────────────────
        // Agrupa ordens finalizadas por máquina e monta o top 5
        Map<Long, List<OrdemDeProducao>> porMaquina = ordens.stream()
                .filter(o -> o.getStatus() == StatusOrdem.FINALIZADA)
                .collect(Collectors.groupingBy(o -> o.getMaquina().getId()));

        List<DashboardResponse.MaquinaRankingItem> topMaquinas = porMaquina.entrySet().stream()
                .map(entry -> {
                    var maquina = entry.getValue().get(0).getMaquina();
                    long unidades = entry.getValue().stream()
                            .mapToLong(OrdemDeProducao::getQuantidade).sum();
                    return DashboardResponse.MaquinaRankingItem.builder()
                            .maquinaId(maquina.getId())
                            .maquinaNome(maquina.getNome())
                            .ordensFinalizadas(entry.getValue().size())
                            .unidadesProduzidas(unidades)
                            .build();
                })
                .sorted(Comparator.comparingLong(
                        DashboardResponse.MaquinaRankingItem::getOrdensFinalizadas).reversed())
                .limit(5)
                .toList();

        return DashboardResponse.builder()
                .totalMaquinas(totalMaquinas)
                .maquinasAtivas(maquinasAtivas)
                .maquinasInativas(maquinasInativas)
                .maquinasEmManutencao(maquinasManutencao)
                .totalProdutos(totalProdutos)
                .totalOrdens(totalOrdens)
                .ordensPendentes(ordensPendentes)
                .ordensEmProducao(ordensEmProducao)
                .ordensFinalizada(ordensFinalizada)
                .totalUnidadesProduzidas(totalUnidadesProduzidas)
                .totalUnidadesEmAberto(totalUnidadesEmAberto)
                .topMaquinas(topMaquinas)
                .build();
    }
}