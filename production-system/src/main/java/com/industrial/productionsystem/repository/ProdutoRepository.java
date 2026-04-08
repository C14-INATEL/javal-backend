package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}