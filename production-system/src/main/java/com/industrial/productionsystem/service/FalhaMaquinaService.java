package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.FalhaMaquinaRequest;
import com.industrial.productionsystem.dto.FalhaMaquinaResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.FalhaMaquina;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.FalhaMaquinaRepository;
import com.industrial.productionsystem.repository.MaquinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FalhaMaquinaService {

    private final FalhaMaquinaRepository repository;
    private final MaquinaRepository maquinaRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public FalhaMaquinaResponse registrar(FalhaMaquinaRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));

        Maquina maquina = maquinaRepository.findByIdAndCompanyId(request.getMaquinaId(), companyId)
                .orElseThrow(() -> new NotFoundException("Máquina não encontrada"));

        FalhaMaquina falha = construirFalha(request, maquina, company);
        FalhaMaquina salva = repository.save(falha);

        colocarMaquinaEmManutencao(maquina);

        return FalhaMaquinaResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public List<FalhaMaquinaResponse> listar(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .map(FalhaMaquinaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FalhaMaquinaResponse> listarPorMaquina(Long maquinaId, Long companyId) {
        return repository.findByMaquinaIdAndCompanyId(maquinaId, companyId)
                .stream()
                .map(FalhaMaquinaResponse::from)
                .toList();
    }

    @Transactional
    public FalhaMaquinaResponse resolver(Long id, Long companyId) {

        FalhaMaquina falha = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Falha não encontrada"));

        falha.setStatus(StatusFalha.RESOLVIDA);
        falha.setDataResolucao(LocalDateTime.now());
        FalhaMaquina salva = repository.save(falha);

        tentarReativarMaquina(falha.getMaquina());

        return FalhaMaquinaResponse.from(salva);
    }

    private FalhaMaquina construirFalha(FalhaMaquinaRequest request, Maquina maquina, Company company) {
        FalhaMaquina falha = new FalhaMaquina();
        falha.setDescricao(request.getDescricao());
        falha.setSeveridade(request.getSeveridade());
        falha.setStatus(StatusFalha.ABERTA);
        falha.setDataAbertura(LocalDateTime.now());
        falha.setMaquina(maquina);
        falha.setCompany(company);
        return falha;
    }

    private void colocarMaquinaEmManutencao(Maquina maquina) {
        if (maquina.getStatus() != StatusMaquina.MANUTENCAO) {
            maquina.setStatusAnteriorManutencao(maquina.getStatus());
        }

        maquina.setStatus(StatusMaquina.MANUTENCAO);
        maquinaRepository.save(maquina);
    }

    private void tentarReativarMaquina(Maquina maquina) {
        boolean aindaTemFalhaAberta =
                repository.existsByMaquinaIdAndStatus(maquina.getId(), StatusFalha.ABERTA);

        if (!aindaTemFalhaAberta) {
            StatusMaquina statusRestaurado = maquina.getStatusAnteriorManutencao() != null
                    ? maquina.getStatusAnteriorManutencao()
                    : StatusMaquina.ATIVA;

            maquina.setStatus(statusRestaurado);
            maquina.setStatusAnteriorManutencao(null);
            maquinaRepository.save(maquina);
        }
    }
}