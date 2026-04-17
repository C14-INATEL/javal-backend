package com.industrial.productionsystem.entity;

import com.industrial.productionsystem.entity.enums.StatusOrdem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeProducaoTest {

    // ========== TESTE 4 ==========
    @Test
    @DisplayName("Deve criar ordem de produção com status inicial PENDENTE")
    void deveCriarOrdemDeProducaoComStatusPendente() {

        // Arrange
        Produto produto = criarProdutoValido();
        Maquina maquina = criarMaquinaValida();

        // Act
        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setQuantidade(500);
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setMaquina(maquina);
        ordem.setDataInicio(LocalDateTime.of(2026, 4, 7, 8, 0));

        // Assert
        assertEquals(produto, ordem.getProduto());
        assertEquals(500, ordem.getQuantidade());
        assertEquals("Torno CNC 01", ordem.getMaquina().getNome());
        assertEquals(LocalDateTime.of(2026, 4, 7, 8, 0), ordem.getDataInicio());
        assertNull(ordem.getDataFim());

        assertEquals(StatusOrdem.PENDENTE, ordem.getStatus());
        assertNotEquals(StatusOrdem.EM_PRODUCAO, ordem.getStatus());
        assertNotEquals(StatusOrdem.FINALIZADA, ordem.getStatus());
        assertNotEquals(StatusOrdem.CANCELADA, ordem.getStatus());
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private Produto criarProdutoValido() {
        Produto produto = new Produto();
        produto.setNome("Engrenagem Industrial");
        produto.setTempoProducaoUnitario(30);
        return produto;
    }

    private Maquina criarMaquinaValida() {
        return new Maquina("Torno CNC 01", "Torno CNC", 100);
    }
}