package com.industrial.production_system;

import com.industrial.production_system.entity.*;
import com.industrial.production_system.repository.ProdutoRepository;
import com.industrial.production_system.repository.OrdemDeProducaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrdemDeProducaoTest {

    @Autowired
    private OrdemDeProducaoRepository ordemRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    @Test
    void deveSalvarOrdem() {
        Produto produto = new Produto();
        produto.setNome("Produto B");
        produto.setTempoProducaoUnitario(5);
        produto = produtoRepo.save(produto);

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setQuantidade(100);
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setMaquinaResponsavel("Máquina 1");
        ordem.setDataInicio(LocalDateTime.now());

        OrdemDeProducao salva = ordemRepo.save(ordem);

        assertNotNull(salva.getId());
        assertEquals(StatusOrdem.PENDENTE, salva.getStatus());
    }
}