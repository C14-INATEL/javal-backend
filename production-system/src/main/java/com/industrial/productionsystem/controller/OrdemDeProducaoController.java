package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.service.OrdemDeProducaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens")
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService service;

    public OrdemDeProducaoController(OrdemDeProducaoService service) {
        this.service = service;
    }

    @PostMapping
    public OrdemDeProducao criar(@RequestBody OrdemDeProducao ordem) {
        return service.criar(ordem);
    }

    @GetMapping
    public List<OrdemDeProducao> listar() {
        return service.listar();
    }

    @PostMapping("/{id}/iniciar")
    public OrdemDeProducao iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/{id}/finalizar")
    public OrdemDeProducao finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }
}