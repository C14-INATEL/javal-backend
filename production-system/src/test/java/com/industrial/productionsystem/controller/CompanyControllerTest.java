package com.industrial.productionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.productionsystem.dto.CompanyRegisterRequest;
import com.industrial.productionsystem.dto.CompanyRegisterResponse;
import com.industrial.productionsystem.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@WithMockUser
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @Test
    @DisplayName("POST /api/companies/register - deve retornar 201 e dados da empresa cadastrada")
    void deveCadastrarEmpresaComDadosValidosERetornar201() throws Exception {
        when(companyService.register(any(CompanyRegisterRequest.class)))
                .thenReturn(CompanyTest.criarResponseEsperado());

        mockMvc.perform(post("/api/companies/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CompanyTest.criarRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Indústria ABC"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-99"))
                .andExpect(jsonPath("$.email").value("contato@abc.com.br"));

        verify(companyService, times(1)).register(any(CompanyRegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/companies/register - deve retornar 400 quando nome estiver ausente")
    void deveRetornar400QuandoNomeAusente() throws Exception {
        CompanyRegisterRequest request = CompanyTest.criarRequestValido();
        request.setName(null);

        mockMvc.perform(post("/api/companies/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(companyService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/companies/register - deve retornar 400 quando CNPJ tiver formato inválido")
    void deveRetornar400QuandoCnpjFormatoInvalido() throws Exception {
        CompanyRegisterRequest request = CompanyTest.criarRequestValido();
        request.setCnpj("12345678000199");

        mockMvc.perform(post("/api/companies/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(companyService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/companies/register - deve retornar 400 quando email for inválido")
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        CompanyRegisterRequest request = CompanyTest.criarRequestValido();
        request.setEmail("email-sem-arroba");

        mockMvc.perform(post("/api/companies/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(companyService, never()).register(any());
    }

    static class CompanyTest {

        static CompanyRegisterRequest criarRequestValido() {
            CompanyRegisterRequest request = new CompanyRegisterRequest();
            request.setName("Indústria ABC");
            request.setCnpj("12.345.678/0001-99");
            request.setEmail("contato@abc.com.br");
            request.setPhone("(35) 99999-0000");
            request.setResponsibleName("João Silva");
            request.setPassword("senha123");
            return request;
        }

        static CompanyRegisterResponse criarResponseEsperado() {
            return CompanyRegisterResponse.builder()
                    .id(1L)
                    .name("Indústria ABC")
                    .cnpj("12.345.678/0001-99")
                    .email("contato@abc.com.br")
                    .phone("(35) 99999-0000")
                    .responsibleName("João Silva")
                    .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                    .build();
        }
    }
}