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

import com.technova.technov.domain.dto.EnvioDto;
import com.technova.technov.domain.service.EnvioService;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin("*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public ResponseEntity<List<EnvioDto>> listarTodos() {
        List<EnvioDto> envios = envioService.listarTodos();
        return ResponseEntity.ok(envios);
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<EnvioDto>> listarPorVenta(@PathVariable Integer ventaId) {
        List<EnvioDto> envios = envioService.listarPorVenta(ventaId);
        return ResponseEntity.ok(envios);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<EnvioDto> obtenerPorId(@PathVariable Integer id) {
        EnvioDto envio = envioService.detalle(id);
        if (envio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(envio);
    }

    @PostMapping
    public ResponseEntity<EnvioDto> crear(@RequestBody EnvioDto envioDto) {
        EnvioDto creado = envioService.crear(envioDto);
        return ResponseEntity.ok(creado);
    }

   
    @PutMapping("/{id}")
    public ResponseEntity<EnvioDto> actualizarEnvio(@PathVariable Integer id, @RequestBody EnvioDto envioDto) {
        EnvioDto envioActualizado = envioService.actualizar(id, envioDto);
        if (envioActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(envioActualizado);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnvio(@PathVariable Integer id) {
        boolean eliminarEnvio = envioService.eliminar(id);
        if (!eliminarEnvio) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

