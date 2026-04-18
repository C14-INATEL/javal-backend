package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.repository.MaquinaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @Mock
    private MaquinaRepository repository;

    @InjectMocks
    private MaquinaService service;

    @Test
    void deveCriarMaquina() {
        Maquina maquina = new Maquina("Máquina 1", "CNC", 100);

        when(repository.save(maquina)).thenReturn(maquina);

        Maquina resultado = service.criar(maquina);

        assertNotNull(resultado);
        assertEquals("Máquina 1", resultado.getNome());

        verify(repository, times(1)).save(maquina);
    }

    @Test
    void deveListarMaquinas() {
        when(repository.findAll()).thenReturn(List.of(new Maquina(), new Maquina()));

        List<Maquina> lista = service.listar();

        assertEquals(2, lista.size());

        verify(repository, times(1)).findAll();
    }

    @Test
    void deveAtualizarStatusDaMaquina() {
        Maquina maquina = new Maquina("Máquina 1", "CNC", 100);
        maquina.setStatus(StatusMaquina.ATIVA);

        when(repository.findById(1L)).thenReturn(Optional.of(maquina));
        when(repository.save(any())).thenReturn(maquina);

        Maquina atualizada = service.alterarStatus(1L, StatusMaquina.MANUTENCAO);

        assertEquals(StatusMaquina.MANUTENCAO, atualizada.getStatus());

        verify(repository).findById(1L);
        verify(repository).save(maquina);
    }

    @Test
    void deveLancarErroQuandoMaquinaNaoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.alterarStatus(1L, StatusMaquina.MANUTENCAO)
        );

        assertEquals("Máquina não encontrada", exception.getMessage());

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }
}