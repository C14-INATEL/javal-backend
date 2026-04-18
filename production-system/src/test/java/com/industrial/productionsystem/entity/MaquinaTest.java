package com.industrial.productionsystem.entity;

import com.industrial.productionsystem.entity.enums.StatusMaquina;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaquinaTest {

    @Test
    void deveCriarMaquinaComConstrutor() {
        Maquina maquina = new Maquina("Torno CNC", "CNC", 100);

        assertEquals("Torno CNC", maquina.getNome());
        assertEquals("CNC", maquina.getTipo());
        assertEquals(100, maquina.getCapacidadePorHora());
        assertEquals(StatusMaquina.ATIVA, maquina.getStatus());
    }

    @Test
    void deveAlterarStatusDaMaquina() {
        Maquina maquina = new Maquina("Máquina X", "Corte", 50);

        maquina.setStatus(StatusMaquina.MANUTENCAO);

        assertEquals(StatusMaquina.MANUTENCAO, maquina.getStatus());
    }
}