package com.industrial.productionsystem;

import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProdutoTest {

    @Autowired
    private ProdutoRepository repository;

    @Test
    void deveSalvarProduto() {
        Produto p = new Produto();
        p.setNome("Produto A");
        p.setTempoProducaoUnitario(10);

        Produto salvo = repository.save(p);

        assertNotNull(salvo.getId());
        assertEquals("Produto A", salvo.getNome());
    }
}