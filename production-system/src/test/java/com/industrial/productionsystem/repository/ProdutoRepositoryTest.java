package com.industrial.productionsystem.repository;
import java.util.List;
import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class ProdutoRepositoryTest {

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

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirProdutos() {
        List<Produto> produtos = repository.findAll();

        assertTrue(produtos.isEmpty());
    }
}