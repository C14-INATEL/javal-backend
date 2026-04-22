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

        if (maquina.getNome() == null || maquina.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da máquina é obrigatório");
        }

        if (maquina.getTipo() == null || maquina.getTipo().isBlank()) {
            throw new IllegalArgumentException("Tipo da máquina é obrigatório");
        }

        if (maquina.getCapacidadePorHora() == null || maquina.getCapacidadePorHora() <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser maior que zero");
        }

        return repository.save(maquina);
    }

    public List<Maquina> listar() {
        return repository.findAll();
    }

    public Maquina alterarStatus(Long id, StatusMaquina status) {

        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }

        Maquina maquina = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada"));

        maquina.setStatus(status);

        return repository.save(maquina);
    }
}