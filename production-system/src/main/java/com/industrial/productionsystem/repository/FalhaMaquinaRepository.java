package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.FalhaMaquina;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FalhaMaquinaRepository extends JpaRepository<FalhaMaquina, Long> {

    List<FalhaMaquina> findByCompanyId(Long companyId);

    List<FalhaMaquina> findByMaquinaIdAndCompanyId(Long maquinaId, Long companyId);

    Optional<FalhaMaquina> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByMaquinaIdAndStatus(Long maquinaId, StatusFalha status);
}