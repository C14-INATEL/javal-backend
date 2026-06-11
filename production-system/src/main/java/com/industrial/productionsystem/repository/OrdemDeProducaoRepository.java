package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdemDeProducaoRepository extends JpaRepository<OrdemDeProducao, Long> {
    List<OrdemDeProducao> findByCompanyId(Long companyId);
    Optional<OrdemDeProducao> findByIdAndCompanyId(Long id, Long companyId);
    List<OrdemDeProducao> findByMaquinaIdAndCompanyId(Long maquinaId, Long companyId);
    boolean existsByMaquinaIdAndCompanyId(Long maquinaId, Long companyId);
    boolean existsByProdutoIdAndCompanyId(Long produtoId, Long companyId);
    long countByCompanyId(Long companyId);

    long countByCompanyIdAndStatus(Long companyId, StatusOrdem status);

    @Query("SELECT COALESCE(SUM(o.quantidade), 0) FROM OrdemDeProducao o " +
            "WHERE o.company.id = :companyId AND o.status = :status")
    long sumQuantidadeByCompanyIdAndStatus(
            @Param("companyId") Long companyId,
            @Param("status") StatusOrdem status);

    @Query("SELECT COALESCE(SUM(o.quantidade), 0) FROM OrdemDeProducao o " +
            "WHERE o.company.id = :companyId AND o.status IN :statuses")
    long sumQuantidadeByCompanyIdAndStatusIn(
            @Param("companyId") Long companyId,
            @Param("statuses") List<StatusOrdem> statuses);

    @Query("SELECT o FROM OrdemDeProducao o " +
            "JOIN FETCH o.maquina " +
            "WHERE o.company.id = :companyId AND o.status = 'FINALIZADA'")
    List<OrdemDeProducao> findFinalizadasComMaquina(@Param("companyId") Long companyId);
}