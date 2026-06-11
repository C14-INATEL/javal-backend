package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.MaquinaRequest;
import com.industrial.productionsystem.dto.MaquinaResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.MaquinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.industrial.productionsystem.repository.FalhaMaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaquinaService {

    private final MaquinaRepository repository;
    private final CompanyRepository companyRepository;
    private final FalhaMaquinaRepository falhaMaquinaRepository;
    private final OrdemDeProducaoRepository ordemDeProducaoRepository;

    public MaquinaResponse criar(MaquinaRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));

        Maquina maquina = new Maquina(
                request.getNome(),
                request.getTipo(),
                request.getCapacidadePorHora(),
                company
        );

        if (request.getStatus() != null) {
            maquina.setStatus(request.getStatus());
        }

        return MaquinaResponse.from(repository.save(maquina));
    }

    public List<MaquinaResponse> listar(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .map(MaquinaResponse::from)
                .toList();
    }

    public MaquinaResponse alterarStatus(Long id, StatusMaquina status, Long companyId) {

        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }

        Maquina maquina = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Máquina não encontrada"));

        maquina.setStatus(status);
        return MaquinaResponse.from(repository.save(maquina));
    }

    public void deletar(Long id, Long companyId) {
        Maquina maquina = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Máquina não encontrada"));

        boolean possuiFalhas =
                falhaMaquinaRepository.existsByMaquinaIdAndCompanyId(id, companyId);

        boolean possuiOrdens =
                ordemDeProducaoRepository.existsByMaquinaIdAndCompanyId(id, companyId);

        if (possuiFalhas || possuiOrdens) {
            throw new IllegalArgumentException(
                    "Não é possível excluir máquina com falhas ou ordens vinculadas");
        }

        repository.delete(maquina);
    }

}