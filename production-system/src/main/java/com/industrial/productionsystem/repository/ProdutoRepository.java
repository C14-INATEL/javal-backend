package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByCompanyId(Long companyId);
    Optional<Produto> findByIdAndCompanyId(Long id, Long companyId);
}