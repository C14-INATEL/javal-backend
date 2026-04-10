package com.industrial.productionsystem.entity;

import com.industrial.productionsystem.entity.enums.StatusOrdem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeProducaoTest {

    // ========== TESTE 4: Criação de ordem de produção e verificação do status inicial ==========
    @Test
    @DisplayName("Deve criar ordem de produção com status inicial PENDENTE")
    void deveCriarOrdemDeProducaoComStatusPendente() {
        // Arrange - criação do produto associado
        Produto produto = new Produto();
        produto.setNome("Engrenagem Industrial");
        produto.setTempoProducaoUnitario(30);

        // Arrange - criação da máquina associada
        Maquina maquina = new Maquina();
        maquina.setNome("Torno CNC 01");

        // Act - criação da ordem de produção
        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setProduto(produto);
        ordem.setQuantidade(500);
        ordem.setStatus(StatusOrdem.PENDENTE);
        ordem.setMaquina(maquina);
        ordem.setDataInicio(LocalDateTime.of(2026, 4, 7, 8, 0));

        // Assert - verifica todos os campos
        assertEquals(produto, ordem.getProduto());
        assertEquals(500, ordem.getQuantidade());
        assertEquals("Torno CNC 01", ordem.getMaquina().getNome());
        assertEquals(LocalDateTime.of(2026, 4, 7, 8, 0), ordem.getDataInicio());
        assertNull(ordem.getDataFim()); // ainda não finalizou

        // Verificação principal: status inicial é PENDENTE
        assertEquals(StatusOrdem.PENDENTE, ordem.getStatus());
        assertNotEquals(StatusOrdem.EM_PRODUCAO, ordem.getStatus());
        assertNotEquals(StatusOrdem.FINALIZADA, ordem.getStatus());
        assertNotEquals(StatusOrdem.CANCELADA, ordem.getStatus());
    }
}