package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.ProdutoRequest;
import com.industrial.productionsystem.dto.ProdutoResponse;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(
            @Valid @RequestBody ProdutoRequest request,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        ProdutoResponse response = service.criar(request, principal.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar(
            @AuthenticationPrincipal CompanyPrincipal principal) {

        return ResponseEntity.ok(service.listar(principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal principal) {

        service.deletar(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}