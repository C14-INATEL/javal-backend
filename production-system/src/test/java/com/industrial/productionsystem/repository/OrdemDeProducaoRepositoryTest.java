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
import java.util.List;

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

        Produto produto = produtoRepo.save(criarProdutoValido());
        Maquina maquina = maquinaRepo.save(criarMaquinaValida());

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

    @Test
    void deveBuscarOrdensPorMaquina() {

        Produto produto = produtoRepo.save(criarProdutoValido());
        Maquina maquina = maquinaRepo.save(criarMaquinaValida());

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setQuantidade(50);
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setMaquina(maquina);

        ordemRepo.save(ordem);

        List<OrdemDeProducao> resultado =
                ordemRepo.findByMaquinaId(maquina.getId());

        assertFalse(resultado.isEmpty());
        assertEquals(maquina.getId(),
                resultado.get(0).getMaquina().getId());
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private Produto criarProdutoValido() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setTempoProducaoUnitario(5);
        return produto;
    }

    private Maquina criarMaquinaValida() {
        return new Maquina("Máquina Teste", "CNC", 100);
    }
}