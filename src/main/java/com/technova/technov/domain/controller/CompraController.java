package com.technova.technov.domain.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.CompraRequestDto;
import com.technova.technov.domain.service.ComprasService;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin("*")
public class CompraController {

    private final ComprasService comprasService;

    public CompraController(ComprasService comprasService) {
        this.comprasService = comprasService;
    }

    @GetMapping
    public ResponseEntity<List<CompraDto>> listarTodos() {
        List<CompraDto> compras = comprasService.listar();
        return ResponseEntity.ok(compras);
    }

   
    @GetMapping("/{id}")
    public ResponseEntity<CompraDto> obtenerPorId(@PathVariable Integer id) {
        CompraDto compra = comprasService.detalle(id);
        if (compra == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compra);
    }

    @PostMapping
    public ResponseEntity<CompraDto> crear(@RequestBody CompraRequestDto compraRequestDto) {
        CompraDto creado = comprasService.crear(compraRequestDto);
        return ResponseEntity.ok(creado);
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<CompraDto> actualizarCompra(@PathVariable Integer id, @RequestBody CompraRequestDto compraRequestDto) {
        CompraDto compraActualizada = comprasService.actualizar(id, compraRequestDto);
        if (compraActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compraActualizada);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCompra(@PathVariable Integer id) {
        boolean eliminarCompra = comprasService.eliminar(id);
        if (!eliminarCompra) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

