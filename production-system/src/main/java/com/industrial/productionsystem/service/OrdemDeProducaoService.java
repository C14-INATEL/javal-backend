package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.OrdemRequest;
import com.industrial.productionsystem.dto.OrdemResponse;
import com.industrial.productionsystem.entity.*;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemDeProducaoService {

    private final OrdemDeProducaoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final MaquinaRepository maquinaRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public OrdemResponse criar(OrdemRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));

        Produto produto = produtoRepository.findByIdAndCompanyId(request.getProdutoId(), companyId)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado nesta empresa"));

        Maquina maquina = maquinaRepository.findByIdAndCompanyId(request.getMaquinaId(), companyId)
                .orElseThrow(() -> new NotFoundException("Máquina não encontrada nesta empresa"));

        if (maquina.getStatus() == StatusMaquina.INATIVA) {
            throw new IllegalArgumentException("Máquina está inativa e não pode receber ordens");
        }

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setMaquina(maquina);
        ordem.setQuantidade(request.getQuantidade());
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setCompany(company);

        return OrdemResponse.from(repository.save(ordem));
    }

    @Transactional(readOnly = true)
    public List<OrdemResponse> listar(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .map(OrdemResponse::from)
                .toList();
    }

    @Transactional
    public OrdemResponse iniciar(Long id, Long companyId) {
        OrdemDeProducao ordem = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Ordem não encontrada"));

        if (ordem.getStatus() != StatusOrdem.PENDENTE) {
            throw new IllegalArgumentException("Apenas ordens PENDENTES podem ser iniciadas");
        }

        if (ordem.getMaquina().getStatus() == StatusMaquina.MANUTENCAO) {
            throw new IllegalArgumentException("Máquina em manutenção, não é possível iniciar a ordem");
        }

        ordem.setStatus(StatusOrdem.EM_PRODUCAO);
        ordem.setDataInicio(LocalDateTime.now());

        return OrdemResponse.from(repository.save(ordem));
    }

    @Transactional
    public OrdemResponse finalizar(Long id, Long companyId) {
        OrdemDeProducao ordem = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Ordem não encontrada"));

        if (ordem.getStatus() != StatusOrdem.EM_PRODUCAO) {
            throw new IllegalArgumentException("Apenas ordens EM_PRODUCAO podem ser finalizadas");
        }

        ordem.setStatus(StatusOrdem.FINALIZADA);
        ordem.setDataFim(LocalDateTime.now());

        return OrdemResponse.from(repository.save(ordem));
    }

    @Transactional
    public OrdemResponse cancelar(Long id, Long companyId) {

        OrdemDeProducao ordem = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Ordem não encontrada"));

        if (ordem.getStatus() != StatusOrdem.PENDENTE) {
            throw new IllegalArgumentException(
                    "Apenas ordens PENDENTES podem ser canceladas");
        }

        ordem.setStatus(StatusOrdem.CANCELADA);
        ordem.setDataFim(LocalDateTime.now());

        return OrdemResponse.from(repository.save(ordem));
    }
}