package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.DashboardResponse;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     * Retorna as métricas da empresa autenticada.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CompanyPrincipal principal) {

        DashboardResponse response = dashboardService.getDashboard(principal.getId());
        return ResponseEntity.ok(response);
    }
}