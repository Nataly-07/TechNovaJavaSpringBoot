package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.MensajeEmpleadoDto;
import com.technova.technov.domain.service.MensajeEmpleadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes-empleado")
@CrossOrigin("*")
public class MensajeEmpleadoController {

    private final MensajeEmpleadoService mensajeEmpleadoService;

    public MensajeEmpleadoController(MensajeEmpleadoService mensajeEmpleadoService) {
        this.mensajeEmpleadoService = mensajeEmpleadoService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeEmpleadoDto>> listarTodos() {
        List<MensajeEmpleadoDto> mensajes = mensajeEmpleadoService.listarTodos();
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<MensajeEmpleadoDto>> listarPorEmpleado(@PathVariable Long empleadoId) {
        List<MensajeEmpleadoDto> mensajes = mensajeEmpleadoService.listarPorEmpleado(empleadoId);
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<MensajeEmpleadoDto>> listarPorTipoYPrioridad(
            @RequestParam String tipo,
            @RequestParam String prioridad) {
        List<MensajeEmpleadoDto> mensajes = mensajeEmpleadoService.listarPorTipoYPrioridad(tipo, prioridad);
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping
    public ResponseEntity<MensajeEmpleadoDto> crear(@RequestBody MensajeEmpleadoDto mensajeEmpleadoDto) {
        MensajeEmpleadoDto creado = mensajeEmpleadoService.crear(mensajeEmpleadoDto);
        return ResponseEntity.ok(creado);
    }
}


