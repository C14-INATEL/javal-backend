package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.CompanyRegisterRequest;
import com.industrial.productionsystem.dto.CompanyRegisterResponse;
import com.industrial.productionsystem.dto.LoginRequest;
import com.industrial.productionsystem.dto.LoginResponse;
import com.industrial.productionsystem.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/register")
    public ResponseEntity<CompanyRegisterResponse> register(
            @Valid @RequestBody CompanyRegisterRequest request) {

        CompanyRegisterResponse response = companyService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = companyService.login(request);
        return ResponseEntity.ok(response);
    }
}