package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdemResponse {

    private Long id;
    private Long produtoId;
    private String produtoNome;
    private Long maquinaId;
    private String maquinaNome;
    private Integer quantidade;
    private StatusOrdem status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Long companyId;

    public static OrdemResponse from(OrdemDeProducao o) {
        OrdemResponse r = new OrdemResponse();
        r.id = o.getId();
        r.produtoId = o.getProduto().getId();
        r.produtoNome = o.getProduto().getNome();
        r.maquinaId = o.getMaquina().getId();
        r.maquinaNome = o.getMaquina().getNome();
        r.quantidade = o.getQuantidade();
        r.status = o.getStatus();
        r.dataInicio = o.getDataInicio();
        r.dataFim = o.getDataFim();
        r.companyId = o.getCompany().getId();
        return r;
    }
}