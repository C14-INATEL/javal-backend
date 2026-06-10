package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.OrdemRequest;
import com.industrial.productionsystem.dto.OrdemResponse;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.OrdemDeProducaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens")
@RequiredArgsConstructor
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService service;

    @PostMapping
    public ResponseEntity<OrdemResponse> criar(
            @Valid @RequestBody OrdemRequest request,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        OrdemResponse response = service.criar(request, principal.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrdemResponse>> listar(
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.listar(principal.getId()));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<OrdemResponse> iniciar(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.iniciar(id, principal.getId()));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<OrdemResponse> finalizar(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.finalizar(id, principal.getId()));
    }
}