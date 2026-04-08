package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.repository.MaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemDeProducaoService {

    private final OrdemDeProducaoRepository repository;
    private final MaquinaRepository maquinaRepository;

    public OrdemDeProducaoService(OrdemDeProducaoRepository repository,
                                  MaquinaRepository maquinaRepository) {
        this.repository = repository;
        this.maquinaRepository = maquinaRepository;
    }

    public OrdemDeProducao criar(OrdemDeProducao ordem) {
        ordem.setStatus("PENDENTE");
        return repository.save(ordem);
    }

    public List<OrdemDeProducao> listar() {
        return repository.findAll();
    }

    public OrdemDeProducao iniciar(Long id) {
        OrdemDeProducao ordem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        Maquina maquina = ordem.getMaquina();

        if (maquina.getStatus().equals("MANUTENCAO")) {
            throw new RuntimeException("Máquina em manutenção");
        }

        ordem.setStatus("EM_PRODUCAO");
        ordem.setDataInicio(LocalDateTime.now());

        return repository.save(ordem);
    }

    public OrdemDeProducao finalizar(Long id) {
        OrdemDeProducao ordem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        ordem.setStatus("FINALIZADA");
        ordem.setDataFim(LocalDateTime.now());

        return repository.save(ordem);
    }
}