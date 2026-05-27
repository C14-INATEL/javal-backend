package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdemDeProducaoRepository extends JpaRepository<OrdemDeProducao, Long> {
    List<OrdemDeProducao> findByCompanyId(Long companyId);
    Optional<OrdemDeProducao> findByIdAndCompanyId(Long id, Long companyId);
    List<OrdemDeProducao> findByMaquinaIdAndCompanyId(Long maquinaId, Long companyId);
}