package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.LoginRequest;
import com.industrial.productionsystem.dto.LoginResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceLoginTest {

    private CompanyRepository companyRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);

        companyService = new CompanyService(
                companyRepository,
                passwordEncoder,
                jwtUtil
        );
    }

    @Test
    void deveFazerLoginComSucesso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("empresa@test.com");
        request.setPassword("senha123");

        Company company = Company.builder()
                .id(1L)
                .name("Empresa Teste")
                .email("empresa@test.com")
                .password("senha-criptografada")
                .build();

        when(companyRepository.findByEmail("empresa@test.com"))
                .thenReturn(Optional.of(company));

        when(passwordEncoder.matches("senha123", "senha-criptografada"))
                .thenReturn(true);

        when(jwtUtil.generateToken(1L, "empresa@test.com"))
                .thenReturn("token-gerado");

        LoginResponse response = companyService.login(request);

        assertEquals("token-gerado", response.getToken());
        assertEquals(1L, response.getCompanyId());
        assertEquals("Empresa Teste", response.getCompanyName());
        assertEquals("empresa@test.com", response.getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExiste() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@test.com");
        request.setPassword("senha123");

        when(companyRepository.findByEmail("inexistente@test.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.login(request)
        );

        assertEquals("Email ou senha inválidos", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaForInvalida() {
        LoginRequest request = new LoginRequest();
        request.setEmail("empresa@test.com");
        request.setPassword("senha-errada");

        Company company = Company.builder()
                .id(1L)
                .email("empresa@test.com")
                .password("senha-criptografada")
                .build();

        when(companyRepository.findByEmail("empresa@test.com"))
                .thenReturn(Optional.of(company));

        when(passwordEncoder.matches("senha-errada", "senha-criptografada"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.login(request)
        );

        assertEquals("Email ou senha inválidos", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }
}