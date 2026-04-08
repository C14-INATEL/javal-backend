package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.repository.MaquinaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaquinaService {

    private final MaquinaRepository repository;

    public MaquinaService(MaquinaRepository repository) {
        this.repository = repository;
    }

    public Maquina criar(Maquina maquina) {
        return repository.save(maquina);
    }

    public List<Maquina> listar() {
        return repository.findAll();
    }

    public Maquina alterarStatus(Long id, StatusMaquina status) {
        Maquina maquina = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada"));

        maquina.setStatus(status);

        return repository.save(maquina);
    }
}