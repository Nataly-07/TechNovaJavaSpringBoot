package com.technova.technov.domain.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.ProveedorDto;
import com.technova.technov.domain.service.ProveedorService;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin("*")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorDto>> listarTodos() {
        List<ProveedorDto> proveedores = proveedorService.listarProveedores();
        return ResponseEntity.ok(proveedores);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDto> obtenerPorId(@PathVariable Integer id) {
        ProveedorDto proveedor = proveedorService.proveedorPorId(id).orElse(null);
        if (proveedor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(proveedor);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProveedorDto> activarDesactivarProveedor(@PathVariable Integer id, @RequestBody java.util.Map<String, Boolean> request) {
        Boolean activar = request.get("activar");
        if (activar == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean resultado = proveedorService.activarDesactivarProveedor(id, activar);
        if (!resultado) {
            return ResponseEntity.notFound().build();
        }
        ProveedorDto proveedor = proveedorService.proveedorPorId(id).orElse(null);
        return ResponseEntity.ok(proveedor);
    }

    @PostMapping
    public ResponseEntity<ProveedorDto> crear(@RequestBody ProveedorDto proveedorDto) {
        ProveedorDto creado = proveedorService.crearProveedor(proveedorDto);
        return ResponseEntity.ok(creado);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDto> actualizarProveedor(@PathVariable Integer id, @RequestBody ProveedorDto proveedorDto) {
        ProveedorDto proveedorActualizado = proveedorService.actualizarProveedor(id, proveedorDto);
        if (proveedorActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(proveedorActualizado);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Integer id) {
        boolean eliminarProveedor = proveedorService.eliminarProveedor(id);
        if (!eliminarProveedor) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

