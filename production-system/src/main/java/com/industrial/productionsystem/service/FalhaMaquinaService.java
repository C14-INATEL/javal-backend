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
@Transactional
public class FalhaMaquinaService {

    private final FalhaMaquinaRepository repository;
    private final MaquinaRepository maquinaRepository;
    private final CompanyRepository companyRepository;

    public FalhaMaquinaResponse registrar(FalhaMaquinaRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));

        Maquina maquina = maquinaRepository.findByIdAndCompanyId(request.getMaquinaId(), companyId)
                .orElseThrow(() -> new NotFoundException("Máquina não encontrada"));

        FalhaMaquina falha = new FalhaMaquina();
        falha.setDescricao(request.getDescricao());
        falha.setSeveridade(request.getSeveridade());
        falha.setStatus(StatusFalha.ABERTA);
        falha.setDataAbertura(LocalDateTime.now());
        falha.setMaquina(maquina);
        falha.setCompany(company);

        FalhaMaquina salva = repository.save(falha);

        // Regra de negócio: registrar uma falha coloca a máquina em manutenção
        maquina.setStatus(StatusMaquina.MANUTENCAO);
        maquinaRepository.save(maquina);

        return FalhaMaquinaResponse.from(salva);
    }

    public List<FalhaMaquinaResponse> listar(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .map(FalhaMaquinaResponse::from)
                .toList();
    }

    public List<FalhaMaquinaResponse> listarPorMaquina(Long maquinaId, Long companyId) {
        return repository.findByMaquinaIdAndCompanyId(maquinaId, companyId)
                .stream()
                .map(FalhaMaquinaResponse::from)
                .toList();
    }

    public FalhaMaquinaResponse resolver(Long id, Long companyId) {

        FalhaMaquina falha = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Falha não encontrada"));

        falha.setStatus(StatusFalha.RESOLVIDA);
        falha.setDataResolucao(LocalDateTime.now());
        FalhaMaquina salva = repository.save(falha);

        // Regra de negócio: se não houver mais nenhuma falha aberta,
        // a máquina volta a ficar ativa
        Maquina maquina = falha.getMaquina();
        boolean aindaTemFalhaAberta =
                repository.existsByMaquinaIdAndStatus(maquina.getId(), StatusFalha.ABERTA);

        if (!aindaTemFalhaAberta) {
            maquina.setStatus(StatusMaquina.ATIVA);
            maquinaRepository.save(maquina);
        }

        return FalhaMaquinaResponse.from(salva);
    }
}