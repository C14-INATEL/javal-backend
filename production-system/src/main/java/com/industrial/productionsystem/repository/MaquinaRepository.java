package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
}