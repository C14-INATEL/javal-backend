package com.industrial.production_system;

import com.industrial.production_system.entity.*;
import com.industrial.production_system.repository.OrdemDeProducaoRepository;
import com.industrial.production_system.repository.MaquinaRepository;
import com.industrial.production_system.service.OrdemDeProducaoService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeProducaoServiceTest {

    private final OrdemDeProducaoRepository ordemRepo = Mockito.mock(OrdemDeProducaoRepository.class);
    private final MaquinaRepository maquinaRepo = Mockito.mock(MaquinaRepository.class);

    private final OrdemDeProducaoService service =
            new OrdemDeProducaoService(ordemRepo, maquinaRepo);

    @Test
    void naoDeveIniciarOrdemComMaquinaEmManutencao() {
        Maquina maquina = new Maquina();
        maquina.setStatus("MANUTENCAO");

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setMaquina(maquina);

        Mockito.when(ordemRepo.findById(1L)).thenReturn(Optional.of(ordem));

        assertThrows(RuntimeException.class, () -> service.iniciar(1L));
    }

    @Test
    void deveIniciarOrdemComMaquinaAtiva() {
        Maquina maquina = new Maquina();
        maquina.setStatus("ATIVA");

        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setMaquina(maquina);

        Mockito.when(ordemRepo.findById(1L)).thenReturn(Optional.of(ordem));
        Mockito.when(ordemRepo.save(ordem)).thenReturn(ordem);

        OrdemDeProducao resultado = service.iniciar(1L);

        assertEquals(StatusOrdem.EM_PRODUCAO, resultado.getStatus());
    }

    @Test
    void deveFinalizarOrdem() {
        OrdemDeProducao ordem = new OrdemDeProducao();

        Mockito.when(ordemRepo.findById(1L)).thenReturn(Optional.of(ordem));
        Mockito.when(ordemRepo.save(ordem)).thenReturn(ordem);

        OrdemDeProducao resultado = service.finalizar(1L);

        assertEquals(StatusOrdem.FINALIZADA, resultado.getStatus());
    }
}