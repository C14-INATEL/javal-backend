package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.CompanyRegisterRequest;
import com.industrial.productionsystem.dto.CompanyRegisterResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new CompanyRegisterRequest();
        request.setName("Indústria ABC");
        request.setEmail("contato@industriaabc.com");
        request.setPhone("(35) 99999-0000");
        request.setResponsibleName("João Silva");
        request.setPassword("senha123");
        request.setCnpj("12345678000199");
    }

    @Test
    @DisplayName("Deve cadastrar empresa com sucesso quando email não existe")
    void deveCadastrarEmpresaComSucesso() {

        // Arrange
        when(companyRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(companyRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");

        Company companySalva = Company.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .responsibleName(request.getResponsibleName())
                .password("senhaCriptografada")
                .createdAt(LocalDateTime.now())
                .build();

        when(companyRepository.save(any(Company.class))).thenReturn(companySalva);

        // Act
        CompanyRegisterResponse response = companyService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Indústria ABC", response.getName());
        assertEquals("contato@industriaabc.com", response.getEmail());
        assertEquals("(35) 99999-0000", response.getPhone());
        assertEquals("João Silva", response.getResponsibleName());
        assertNotNull(response.getCreatedAt());

        verify(companyRepository, times(1)).existsByEmail(request.getEmail());
        verify(companyRepository, times(1)).existsByCnpj(request.getCnpj());
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar empresa com email já existente")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {

        // Arrange
        when(companyRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.register(request)
        );

        assertEquals("Email já cadastrado", exception.getMessage());

        verify(companyRepository, never()).save(any(Company.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar empresa com CNPJ já existente")
    void deveLancarExcecaoQuandoCnpjJaCadastrado() {

        // Arrange - email livre, mas CNPJ já cadastrado
        when(companyRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(companyRepository.existsByCnpj(request.getCnpj())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.register(request)
        );

        assertEquals("CNPJ já cadastrado", exception.getMessage());

        // Garante que a verificação de email aconteceu ANTES da de CNPJ
        verify(companyRepository, times(1)).existsByEmail(request.getEmail());
        verify(companyRepository, times(1)).existsByCnpj(request.getCnpj());

        // Nada de criptografar senha ou salvar quando há duplicidade
        verify(passwordEncoder, never()).encode(anyString());
        verify(companyRepository, never()).save(any(Company.class));
    }
}