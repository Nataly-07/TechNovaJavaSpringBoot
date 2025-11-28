package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.TransportadoraDto;
import com.technova.technov.domain.service.TransportadoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportadoras")
@CrossOrigin("*")
public class TransportadoraController {

    private final TransportadoraService transportadoraService;

    public TransportadoraController(TransportadoraService transportadoraService) {
        this.transportadoraService = transportadoraService;
    }

    @GetMapping
    public ResponseEntity<List<TransportadoraDto>> listarTodos() {
        List<TransportadoraDto> transportadoras = transportadoraService.listarTodos();
        return ResponseEntity.ok(transportadoras);
    }

    @GetMapping("/envio/{envioId}")
    public ResponseEntity<List<TransportadoraDto>> listarPorEnvio(@PathVariable Integer envioId) {
        List<TransportadoraDto> transportadoras = transportadoraService.listarPorEnvio(envioId);
        return ResponseEntity.ok(transportadoras);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<TransportadoraDto> obtenerPorId(@PathVariable Integer id) {
        TransportadoraDto transportadora = transportadoraService.detalle(id);
        if (transportadora == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transportadora);
    }

    @PostMapping
    public ResponseEntity<TransportadoraDto> crear(@RequestBody TransportadoraDto transportadoraDto) {
        TransportadoraDto creado = transportadoraService.crear(transportadoraDto);
        return ResponseEntity.ok(creado);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<TransportadoraDto> actualizarTransportadora(@PathVariable Integer id, @RequestBody TransportadoraDto transportadoraDto) {
        TransportadoraDto transportadoraActualizada = transportadoraService.actualizar(id, transportadoraDto);
        if (transportadoraActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transportadoraActualizada);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTransportadora(@PathVariable Integer id) {
        boolean eliminarTransportadora = transportadoraService.eliminar(id);
        if (!eliminarTransportadora) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

