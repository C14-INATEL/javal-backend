package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.FalhaMaquinaRequest;
import com.industrial.productionsystem.dto.FalhaMaquinaResponse;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.FalhaMaquinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/falhas")
@RequiredArgsConstructor
public class FalhaMaquinaController {

    private final FalhaMaquinaService service;

    @PostMapping
    public ResponseEntity<FalhaMaquinaResponse> registrar(
            @Valid @RequestBody FalhaMaquinaRequest request,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        FalhaMaquinaResponse response = service.registrar(request, principal.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FalhaMaquinaResponse>> listar(
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.listar(principal.getId()));
    }

    @GetMapping("/maquina/{maquinaId}")
    public ResponseEntity<List<FalhaMaquinaResponse>> listarPorMaquina(
            @PathVariable Long maquinaId,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.listarPorMaquina(maquinaId, principal.getId()));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<FalhaMaquinaResponse> resolver(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.resolver(id, principal.getId()));
    }
}