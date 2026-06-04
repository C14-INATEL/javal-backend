package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.FalhaMaquina;
import com.industrial.productionsystem.entity.enums.SeveridadeFalha;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FalhaMaquinaResponse {

    private Long id;
    private String descricao;
    private SeveridadeFalha severidade;
    private StatusFalha status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataResolucao;
    private Long maquinaId;
    private String maquinaNome;
    private Long companyId;

    public static FalhaMaquinaResponse from(FalhaMaquina f) {
        FalhaMaquinaResponse r = new FalhaMaquinaResponse();
        r.id = f.getId();
        r.descricao = f.getDescricao();
        r.severidade = f.getSeveridade();
        r.status = f.getStatus();
        r.dataAbertura = f.getDataAbertura();
        r.dataResolucao = f.getDataResolucao();
        r.maquinaId = f.getMaquina().getId();
        r.maquinaNome = f.getMaquina().getNome();
        r.companyId = f.getCompany().getId();
        return r;
    }
}