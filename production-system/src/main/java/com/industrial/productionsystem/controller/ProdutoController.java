package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.service.ProdutoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto) {
        return service.criar(produto);
    }

    @GetMapping
    public List<Produto> listar() {
        return service.listar();
    }
}