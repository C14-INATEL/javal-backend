package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.CompanyRegisterRequest;
import com.industrial.productionsystem.dto.CompanyRegisterResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanyRegisterResponse register(CompanyRegisterRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Company company = Company.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .responsibleName(request.getResponsibleName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Company saved = companyRepository.save(company);

        return CompanyRegisterResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .responsibleName(saved.getResponsibleName())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}