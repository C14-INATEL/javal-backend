package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);
}