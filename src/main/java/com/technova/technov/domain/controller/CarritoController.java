package com.technova.technov.domain.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin("*")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<CarritoItemDto>> listar(@PathVariable Integer usuarioId) {
        List<CarritoItemDto> items = carritoService.listar(usuarioId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{usuarioId}/agregar")
    public ResponseEntity<?> agregar(
            @PathVariable Integer usuarioId,
            @RequestParam Integer productoId,
            @RequestParam(required = false, defaultValue = "1") Integer cantidad) {
        try {
            List<CarritoItemDto> items = carritoService.agregar(usuarioId, productoId, cantidad);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @PutMapping("/{usuarioId}/actualizar")
    public ResponseEntity<List<CarritoItemDto>> actualizar(
            @PathVariable Integer usuarioId,
            @RequestParam Integer detalleId,
            @RequestParam Integer cantidad) {
        List<CarritoItemDto> items = carritoService.actualizar(usuarioId, detalleId, cantidad);
        return ResponseEntity.ok(items);
    }
    
    @DeleteMapping("/{usuarioId}/eliminar/{detalleId}")
    public ResponseEntity<List<CarritoItemDto>> eliminar(
            @PathVariable Integer usuarioId,
            @PathVariable Integer detalleId) {
        List<CarritoItemDto> items = carritoService.eliminar(usuarioId, detalleId);
        return ResponseEntity.ok(items);
    }
    
    @DeleteMapping("/{usuarioId}/vaciar")
    public ResponseEntity<?> vaciar(@PathVariable Integer usuarioId) {
        carritoService.vaciar(usuarioId);
        return ResponseEntity.ok().build();
    }
}

