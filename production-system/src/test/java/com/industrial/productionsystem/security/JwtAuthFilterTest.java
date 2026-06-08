package com.industrial.productionsystem.security;

import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.repository.CompanyRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private CompanyRepository companyRepository;
    private JwtAuthFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        companyRepository = mock(CompanyRepository.class);
        filterChain = mock(FilterChain.class);

        filter = new JwtAuthFilter(jwtUtil, companyRepository);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveContinuarFiltroQuandoNaoExisteHeaderAuthorization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isValid(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveContinuarFiltroQuandoHeaderNaoComecaComBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isValid(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveNaoAutenticarQuandoTokenForInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.isValid("token-invalido")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(companyRepository, never()).findById(anyLong());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveNaoAutenticarQuandoCompanyNaoExiste() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.isValid("token-valido")).thenReturn(true);
        when(jwtUtil.extractCompanyId("token-valido")).thenReturn(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveAutenticarQuandoTokenValidoECompanyExiste() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();

        Company company = Company.builder()
                .id(1L)
                .name("Empresa Teste")
                .email("empresa@test.com")
                .cnpj("12.345.678/0001-90")
                .phone("35999999999")
                .responsibleName("Vinicius")
                .password("hash")
                .build();

        when(jwtUtil.isValid("token-valido")).thenReturn(true);
        when(jwtUtil.extractCompanyId("token-valido")).thenReturn(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof CompanyPrincipal);

        CompanyPrincipal principal = (CompanyPrincipal) authentication.getPrincipal();

        assertEquals(1L, principal.getId());
        assertEquals("empresa@test.com", principal.getEmail());

        verify(filterChain).doFilter(request, response);
    }
}