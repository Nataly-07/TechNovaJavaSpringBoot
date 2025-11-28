package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import com.technova.technov.domain.service.MensajeDirectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes-directos")
@CrossOrigin("*")
public class MensajeDirectoController {

    private final MensajeDirectoService mensajeDirectoService;

    public MensajeDirectoController(MensajeDirectoService mensajeDirectoService) {
        this.mensajeDirectoService = mensajeDirectoService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeDirectoDto>> listarTodos() {
        List<MensajeDirectoDto> mensajes = mensajeDirectoService.listarTodos();
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<MensajeDirectoDto>> listarPorUsuario(@PathVariable Long userId) {
        List<MensajeDirectoDto> mensajes = mensajeDirectoService.listarPorUsuario(userId);
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<MensajeDirectoDto>> listarPorEmpleado(@PathVariable Long empleadoId) {
        List<MensajeDirectoDto> mensajes = mensajeDirectoService.listarPorEmpleado(empleadoId);
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/conversacion/{conversationId}")
    public ResponseEntity<List<MensajeDirectoDto>> listarPorConversacion(@PathVariable String conversationId) {
        List<MensajeDirectoDto> mensajes = mensajeDirectoService.listarPorConversacion(conversationId);
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping
    public ResponseEntity<MensajeDirectoDto> crear(@RequestBody MensajeDirectoDto mensajeDirectoDto) {
        MensajeDirectoDto creado = mensajeDirectoService.crear(mensajeDirectoDto);
        return ResponseEntity.ok(creado);
    }
}

