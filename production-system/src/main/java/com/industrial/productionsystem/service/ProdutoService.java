package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.ProdutoRequest;
import com.industrial.productionsystem.dto.ProdutoResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CompanyRepository companyRepository;

    public ProdutoResponse criar(ProdutoRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));

        Produto produto = new Produto();
        produto.setNome(request.getNome());
        produto.setTempoProducaoUnitario(request.getTempoProducaoUnitario());
        produto.setCompany(company);

        return ProdutoResponse.from(repository.save(produto));
    }

    public List<ProdutoResponse> listar(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    public void deletar(Long id, Long companyId) {
        Produto produto = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        repository.delete(produto);
    }
}