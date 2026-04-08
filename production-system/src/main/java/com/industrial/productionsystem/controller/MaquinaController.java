package com.industrial.productionsystem.controller;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.service.MaquinaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maquinas")
public class MaquinaController {

    private final MaquinaService service;

    public MaquinaController(MaquinaService service) {
        this.service = service;
    }

    @PostMapping
    public Maquina criar(@RequestBody Maquina maquina) {
        return service.criar(maquina);
    }

    @GetMapping
    public List<Maquina> listar() {
        return service.listar();
    }

    @PatchMapping("/{id}/status")
    public Maquina alterarStatus(@PathVariable Long id,
                                 @RequestParam StatusMaquina status) {
        return service.alterarStatus(id, status);
    }
}