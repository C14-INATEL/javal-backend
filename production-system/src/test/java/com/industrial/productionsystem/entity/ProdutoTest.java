package com.industrial.productionsystem.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    // ========== TESTE 3: Criação e manipulação de produto ==========
    @Test
    @DisplayName("Deve criar produto e permitir manipulação dos atributos")
    void deveCriarEManipularProduto() {
        // Arrange - criação do produto
        Produto produto = new Produto();

        // Act - definindo atributos
        produto.setNome("Parafuso M8");
        produto.setTempoProducaoUnitario(15);

        // Assert - verifica valores iniciais
        assertEquals("Parafuso M8", produto.getNome());
        assertEquals(15, produto.getTempoProducaoUnitario());

        // Act - manipulação (alterando atributos)
        produto.setNome("Parafuso M10 Atualizado");
        produto.setTempoProducaoUnitario(20);

        // Assert - verifica valores atualizados
        assertEquals("Parafuso M10 Atualizado", produto.getNome());
        assertEquals(20, produto.getTempoProducaoUnitario());

        // Verifica que id é null antes de persistir (não passou pelo JPA)
        assertNull(produto.getId());
    }
}