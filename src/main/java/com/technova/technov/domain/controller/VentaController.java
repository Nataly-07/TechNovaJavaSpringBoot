package com.technova.technov.domain.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.ResumenVentasDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.dto.VentaRequestDto;
import com.technova.technov.domain.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin("*")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public ResponseEntity<List<VentaDto>> listarTodos() {
        List<VentaDto> ventas = ventaService.listar();
        return ResponseEntity.ok(ventas);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<VentaDto> obtenerPorId(@PathVariable Integer id) {
        VentaDto venta = ventaService.detalle(id);
        if (venta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(venta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaDto>> porUsuario(@PathVariable Integer usuarioId) {
        List<VentaDto> ventas = ventaService.porUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenVentasDto> resumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ResumenVentasDto resumen = ventaService.resumen(desde, hasta);
        return ResponseEntity.ok(resumen);
    }

    @PostMapping
    public ResponseEntity<VentaDto> crear(@RequestBody VentaRequestDto ventaRequestDto) {
        VentaDto creado = ventaService.crear(ventaRequestDto);
        return ResponseEntity.ok(creado);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<VentaDto> actualizarVenta(@PathVariable Integer id, @RequestBody VentaRequestDto ventaRequestDto) {
        VentaDto ventaActualizada = ventaService.actualizar(id, ventaRequestDto);
        if (ventaActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ventaActualizada);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Integer id) {
        boolean eliminarVenta = ventaService.eliminar(id);
        if (!eliminarVenta) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

