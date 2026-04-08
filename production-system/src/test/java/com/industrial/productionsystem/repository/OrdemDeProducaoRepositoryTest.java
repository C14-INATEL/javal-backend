package com.industrial.productionsystem;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.ProdutoRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.industrial.productionsystem.repository.MaquinaRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrdemDeProducaoTest {

    @Autowired
    private OrdemDeProducaoRepository ordemRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    @Autowired
    private MaquinaRepository maquinaRepo;

    @Test
    void deveSalvarOrdem() {
        Produto produto = new Produto();
        produto.setNome("Produto B");
        produto.setTempoProducaoUnitario(5);
        produto = produtoRepo.save(produto);


        Maquina maquina = new Maquina();
        maquina.setNome("Máquina 1");
        maquina = maquinaRepo.save(maquina);

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setQuantidade(100);
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setMaquina(maquina);
        ordem.setDataInicio(LocalDateTime.now());

        OrdemDeProducao salva = ordemRepo.save(ordem);

        assertNotNull(salva.getId());
        assertEquals(StatusOrdem.PENDENTE, salva.getStatus());
    }
}