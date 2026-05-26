package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    List<Maquina> findByCompanyId(Long companyId);
    Optional<Maquina> findByIdAndCompanyId(Long id, Long companyId);
}