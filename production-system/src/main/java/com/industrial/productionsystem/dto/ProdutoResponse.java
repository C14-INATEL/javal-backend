package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.Produto;
import lombok.Data;

@Data
public class ProdutoResponse {

    private Long id;
    private String nome;
    private Integer tempoProducaoUnitario;
    private Long companyId;

    public static ProdutoResponse from(Produto p) {
        ProdutoResponse r = new ProdutoResponse();
        r.id = p.getId();
        r.nome = p.getNome();
        r.tempoProducaoUnitario = p.getTempoProducaoUnitario();
        r.companyId = p.getCompany().getId();
        return r;
    }
}