package com.industrial.productionsystem.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto e permitir manipulação dos atributos")
    void deveCriarEManipularProduto() {
        Produto produto = new Produto();

        produto.setNome("Parafuso M8");
        produto.setTempoProducaoUnitario(15);

        assertEquals("Parafuso M8", produto.getNome());
        assertEquals(15, produto.getTempoProducaoUnitario());

        produto.setNome("Parafuso M10 Atualizado");
        produto.setTempoProducaoUnitario(20);

        assertEquals("Parafuso M10 Atualizado", produto.getNome());
        assertEquals(20, produto.getTempoProducaoUnitario());

        assertNull(produto.getId());
    }
}