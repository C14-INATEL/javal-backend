package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.service.MaquinaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.DisplayName;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaquinaController.class)
@WithMockUser
class MaquinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaquinaService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /maquinas - deve criar máquina com sucesso")
    void deveCriarMaquina() throws Exception {
        Maquina maquina = new Maquina("Máquina 1", "CNC", 100);

        when(service.criar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(maquina);

        mockMvc.perform(post("/maquinas")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(maquina)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Máquina 1"));
    }

    @Test
    @DisplayName("GET /maquinas - deve listar todas as máquinas")
    void deveListarMaquinas() throws Exception {
        when(service.listar()).thenReturn(List.of(new Maquina(), new Maquina()));

        mockMvc.perform(get("/maquinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("PATCH /maquinas/{id}/status - deve alterar status da máquina")
    void deveAlterarStatus() throws Exception {
        Maquina maquina = new Maquina("Máquina 1", "CNC", 100);
        maquina.setStatus(StatusMaquina.MANUTENCAO);

        when(service.alterarStatus(1L, StatusMaquina.MANUTENCAO))
                .thenReturn(maquina);

        mockMvc.perform(patch("/maquinas/1/status")
                        .with(csrf())
                        .param("status", "MANUTENCAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MANUTENCAO"));
    }
}