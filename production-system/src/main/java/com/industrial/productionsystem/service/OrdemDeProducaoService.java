package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemDeProducaoService {

    private final OrdemDeProducaoRepository repository;

    public OrdemDeProducaoService(OrdemDeProducaoRepository repository) {
        this.repository = repository;
    }

    public OrdemDeProducao criar(OrdemDeProducao ordem) {
        ordem.setStatus(StatusOrdem.PENDENTE);
        return repository.save(ordem);
    }

    public List<OrdemDeProducao> listar() {
        return repository.findAll();
    }

    public OrdemDeProducao iniciar(Long id) {
        OrdemDeProducao ordem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        Maquina maquina = ordem.getMaquina();

        if (maquina.getStatus() == StatusMaquina.MANUTENCAO) {
            throw new RuntimeException("Máquina em manutenção");
        }

        ordem.setStatus(StatusOrdem.EM_PRODUCAO);
        ordem.setDataInicio(LocalDateTime.now());

        return repository.save(ordem);
    }

    public OrdemDeProducao finalizar(Long id) {
        OrdemDeProducao ordem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        ordem.setStatus(StatusOrdem.FINALIZADA);
        ordem.setDataFim(LocalDateTime.now());

        return repository.save(ordem);
    }
}