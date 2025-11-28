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

import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.service.CaracteristicaService;

@RestController
@RequestMapping("/api/caracteristicas")
@CrossOrigin("*")
public class CaracteristicaController {

    private final CaracteristicaService caracteristicaService;

    public CaracteristicaController(CaracteristicaService caracteristicaService) {
        this.caracteristicaService = caracteristicaService;
    }

    @GetMapping
    public ResponseEntity<List<CaracteristicasDto>> listarTodos() {
        List<CaracteristicasDto> caracteristicas = caracteristicaService.listar();
        return ResponseEntity.ok(caracteristicas);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<CaracteristicasDto> obtenerPorId(@PathVariable Integer id) {
        CaracteristicasDto caracteristica = caracteristicaService.caracteristicaPorId(id).orElse(null);
        if (caracteristica == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(caracteristica);
    }

    @PostMapping
    public ResponseEntity<CaracteristicasDto> crear(@RequestBody CaracteristicasDto caracteristicasDto) {
        CaracteristicasDto creado = caracteristicaService.crear(caracteristicasDto);
        return ResponseEntity.ok(creado);
    }

   
    @PutMapping("/{id}")
    public ResponseEntity<CaracteristicasDto> actualizarCaracteristica(@PathVariable Integer id, @RequestBody CaracteristicasDto caracteristicasDto) {
        CaracteristicasDto caracteristicaActualizada = caracteristicaService.actualizar(id, caracteristicasDto);
        if (caracteristicaActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(caracteristicaActualizada);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCaracteristica(@PathVariable Integer id) {
        boolean eliminarCaracteristica = caracteristicaService.eliminar(id);
        if (!eliminarCaracteristica) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

