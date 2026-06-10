package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.DashboardResponse;
import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.MaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import com.industrial.productionsystem.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MaquinaRepository maquinaRepository;
    private final ProdutoRepository produtoRepository;
    private final OrdemDeProducaoRepository ordemRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long companyId) {
        log.info("Montando dashboard para empresa={}", companyId);

        long totalMaquinas      = maquinaRepository.countByCompanyId(companyId);
        long maquinasAtivas     = maquinaRepository.countByCompanyIdAndStatus(companyId, StatusMaquina.ATIVA);
        long maquinasInativas   = maquinaRepository.countByCompanyIdAndStatus(companyId, StatusMaquina.INATIVA);
        long maquinasManutencao = maquinaRepository.countByCompanyIdAndStatus(companyId, StatusMaquina.MANUTENCAO);

        long totalProdutos = produtoRepository.countByCompanyId(companyId);

        long totalOrdens      = ordemRepository.countByCompanyId(companyId);
        long ordensPendentes  = ordemRepository.countByCompanyIdAndStatus(companyId, StatusOrdem.PENDENTE);
        long ordensEmProducao = ordemRepository.countByCompanyIdAndStatus(companyId, StatusOrdem.EM_PRODUCAO);
        long ordensFinalizada = ordemRepository.countByCompanyIdAndStatus(companyId, StatusOrdem.FINALIZADA);

        long totalUnidadesProduzidas = ordemRepository
                .sumQuantidadeByCompanyIdAndStatus(companyId, StatusOrdem.FINALIZADA);

        long totalUnidadesEmAberto = ordemRepository
                .sumQuantidadeByCompanyIdAndStatusIn(companyId,
                        List.of(StatusOrdem.PENDENTE, StatusOrdem.EM_PRODUCAO));

        List<OrdemDeProducao> finalizadas = ordemRepository.findFinalizadasComMaquina(companyId);

        Map<Long, List<OrdemDeProducao>> porMaquina = finalizadas.stream()
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