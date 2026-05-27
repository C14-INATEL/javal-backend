package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import lombok.Data;

@Data
public class MaquinaResponse {

    private Long id;
    private String nome;
    private String tipo;
    private StatusMaquina status;
    private Integer capacidadePorHora;
    private Long companyId;

    public static MaquinaResponse from(Maquina m) {
        MaquinaResponse r = new MaquinaResponse();
        r.id = m.getId();
        r.nome = m.getNome();
        r.tipo = m.getTipo();
        r.status = m.getStatus();
        r.capacidadePorHora = m.getCapacidadePorHora();
        r.companyId = m.getCompany().getId();
        return r;
    }
}