package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemDeProducaoRepository extends JpaRepository<OrdemDeProducao, Long> {
    List<OrdemDeProducao> findByMaquinaId(Long maquinaId);
}