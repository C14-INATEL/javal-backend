package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.MaquinaRequest;
import com.industrial.productionsystem.dto.MaquinaResponse;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.MaquinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maquinas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MaquinaController {

    private final MaquinaService service;

    @PostMapping
    public ResponseEntity<MaquinaResponse> criar(
            @Valid @RequestBody MaquinaRequest request,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        MaquinaResponse response = service.criar(request, principal.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MaquinaResponse>> listar(
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.listar(principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MaquinaResponse> alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusMaquina status,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.alterarStatus(id, status, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        service.deletar(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}