package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.CompanyRegisterRequest;
import com.industrial.productionsystem.dto.CompanyRegisterResponse;
import com.industrial.productionsystem.dto.LoginRequest;
import com.industrial.productionsystem.dto.LoginResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public CompanyRegisterResponse register(CompanyRegisterRequest request) {

        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (companyRepository.existsByCnpj(request.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado");
        }

        String senhaCriptografada = passwordEncoder.encode(request.getPassword());

        Company company = Company.builder()
                .name(request.getName())
                .cnpj(request.getCnpj())
                .email(request.getEmail())
                .phone(request.getPhone())
                .responsibleName(request.getResponsibleName())
                .password(senhaCriptografada)
                .build();

        Company saved = companyRepository.save(company);

        return CompanyRegisterResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .cnpj(saved.getCnpj())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .responsibleName(saved.getResponsibleName())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest request) {

        Company company = companyRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), company.getPassword())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(company.getId(), company.getEmail());

        return new LoginResponse(token, company.getId(), company.getName(), company.getEmail());
    }
}