package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.NotificacionDto;
import com.technova.technov.domain.service.NotificacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin("*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDto>> listarTodos() {
        List<NotificacionDto> notificaciones = notificacionService.listarTodos();
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<NotificacionDto>> listarPorUsuario(@PathVariable Long userId) {
        List<NotificacionDto> notificaciones = notificacionService.listarPorUsuario(userId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{userId}/leida")
    public ResponseEntity<List<NotificacionDto>> listarPorUsuarioYLeida(
            @PathVariable Long userId,
            @RequestParam boolean leida) {
        List<NotificacionDto> notificaciones = notificacionService.listarPorUsuarioYLeida(userId, leida);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{userId}/rango")
    public ResponseEntity<List<NotificacionDto>> listarPorUsuarioYRango(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        List<NotificacionDto> notificaciones = notificacionService.listarPorUsuarioYRango(userId, desde, hasta);
        return ResponseEntity.ok(notificaciones);
    }

    @PostMapping
    public ResponseEntity<NotificacionDto> crear(@RequestBody NotificacionDto notificacionDto) {
        NotificacionDto creado = notificacionService.crear(notificacionDto);
        return ResponseEntity.ok(creado);
    }
}

