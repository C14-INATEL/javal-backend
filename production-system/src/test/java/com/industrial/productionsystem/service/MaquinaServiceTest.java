package com.industrial.productionsystem;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.repository.MaquinaRepository;
import com.industrial.productionsystem.service.MaquinaService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class MaquinaServiceTest {

    private final MaquinaRepository repo = Mockito.mock(MaquinaRepository.class);

    private final MaquinaService service =
            new MaquinaService(repo);

    @Test
    void deveAtualizarStatusDaMaquina() {
        Maquina maquina = new Maquina();
        maquina.setStatus(StatusMaquina.ATIVA);

        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(maquina));
        Mockito.when(repo.save(maquina)).thenReturn(maquina);

        Maquina atualizada = service.alterarStatus(1L, StatusMaquina.MANUTENCAO);

        assertEquals(StatusMaquina.MANUTENCAO, atualizada.getStatus());
    }
}