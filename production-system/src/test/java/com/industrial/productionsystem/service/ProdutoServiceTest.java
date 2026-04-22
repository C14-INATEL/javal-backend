package com.industrial.productionsystem.service;

import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveCriarProdutoComSucesso() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setTempoProducaoUnitario(10);

        when(repository.save(any())).thenReturn(produto);

        Produto resultado = produtoService.criar(produto);

        assertNotNull(resultado);
        assertEquals("Produto Teste", resultado.getNome());

        verify(repository, times(1)).save(produto);
    }

    @Test
    void deveLancarErroQuandoTempoInvalido() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setTempoProducaoUnitario(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.criar(produto)
        );

        assertEquals("Tempo de produção deve ser maior que zero", exception.getMessage());

        verify(repository, never()).save(any());
    }
}