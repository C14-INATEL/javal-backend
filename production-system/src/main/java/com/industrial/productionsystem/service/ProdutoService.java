package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto criar(Produto produto) {

        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }

        if (produto.getTempoProducaoUnitario() == null || produto.getTempoProducaoUnitario() <= 0) {
            throw new IllegalArgumentException("Tempo de produção deve ser maior que zero");
        }

        return repository.save(produto);
    }

    public List<Produto> listar() {
        return repository.findAll();
    }
}