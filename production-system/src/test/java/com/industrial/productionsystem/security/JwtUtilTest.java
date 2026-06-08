package com.industrial.productionsystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "javal-secret-key-muito-segura-256bits-ok"
        );

        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void deveGerarTokenValido() {
        String token = jwtUtil.generateToken(1L, "empresa@test.com");

        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void deveExtrairEmailDoToken() {
        String token = jwtUtil.generateToken(1L, "empresa@test.com");

        assertEquals("empresa@test.com", jwtUtil.extractEmail(token));
    }

    @Test
    void deveExtrairCompanyIdDoToken() {
        String token = jwtUtil.generateToken(99L, "empresa@test.com");

        assertEquals(99L, jwtUtil.extractCompanyId(token));
    }

    @Test
    void deveRetornarFalseParaTokenInvalido() {
        assertFalse(jwtUtil.isValid("token-invalido"));
    }

    @Test
    void deveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);

        String token = jwtUtil.generateToken(1L, "empresa@test.com");

        assertFalse(jwtUtil.isValid(token));
    }
}