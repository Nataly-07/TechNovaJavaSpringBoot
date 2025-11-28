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

import com.technova.technov.domain.dto.MedioDePagoDto;
import com.technova.technov.domain.service.MedioDePagoService;

@RestController
@RequestMapping("/api/medios-pago")
@CrossOrigin("*")
public class MedioDePagoController {

    private final MedioDePagoService medioDePagoService;

    public MedioDePagoController(MedioDePagoService medioDePagoService) {
        this.medioDePagoService = medioDePagoService;
    }

    @GetMapping
    public ResponseEntity<List<MedioDePagoDto>> listarTodos() {
        List<MedioDePagoDto> medios = medioDePagoService.listar();
        return ResponseEntity.ok(medios);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<MedioDePagoDto> obtenerPorId(@PathVariable Integer id) {
        MedioDePagoDto medio = medioDePagoService.obtener(id);
        if (medio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medio);
    }

    @PostMapping
    public ResponseEntity<MedioDePagoDto> crear(@RequestBody MedioDePagoDto medioDePagoDto) {
        MedioDePagoDto creado = medioDePagoService.guardar(medioDePagoDto);
        return ResponseEntity.ok(creado);
    }

   
    @PutMapping("/{id}")
    public ResponseEntity<MedioDePagoDto> actualizarMedioDePago(@PathVariable Integer id, @RequestBody MedioDePagoDto medioDePagoDto) {
        medioDePagoDto.setId(id);
        MedioDePagoDto medioActualizado = medioDePagoService.guardar(medioDePagoDto);
        if (medioActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medioActualizado);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMedioDePago(@PathVariable Integer id) {
        boolean eliminarMedioDePago = medioDePagoService.eliminar(id);
        if (!eliminarMedioDePago) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

