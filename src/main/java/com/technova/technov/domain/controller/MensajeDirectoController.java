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

    @PostMapping("/conversacion")
    public ResponseEntity<?> crearConversacion(
            @RequestParam Long userId,
            @RequestParam String asunto,
            @RequestParam String mensaje,
            @RequestParam(required = false, defaultValue = "normal") String prioridad) {
        try {
            MensajeDirectoDto creado = mensajeDirectoService.crearConversacion(userId, asunto, mensaje, prioridad);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDirectoDto> obtenerPorId(@PathVariable Long id) {
        MensajeDirectoDto mensaje = mensajeDirectoService.obtenerPorId(id);
        if (mensaje == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<?> responderMensaje(
            @PathVariable Long id,
            @RequestParam Long senderId,
            @RequestParam String senderType,
            @RequestParam String mensaje) {
        try {
            MensajeDirectoDto respuesta = mensajeDirectoService.responderMensaje(id, senderId, senderType, mensaje);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/{id}/marcar-leido")
    public ResponseEntity<MensajeDirectoDto> marcarLeido(@PathVariable Long id) {
        MensajeDirectoDto mensaje = mensajeDirectoService.marcarLeido(id);
        if (mensaje == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mensaje);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<java.util.Map<String, Object>> obtenerEstadisticas() {
        try {
            java.util.List<MensajeDirectoDto> todosMensajes = mensajeDirectoService.listarTodos();
            if (todosMensajes == null) {
                todosMensajes = new java.util.ArrayList<>();
            }
            
            // Contar conversaciones únicas (no mensajes individuales)
            java.util.Map<String, MensajeDirectoDto> conversacionesUnicas = new java.util.HashMap<>();
            
            for (MensajeDirectoDto mensaje : todosMensajes) {
                if (mensaje.getConversationId() != null) {
                    // Obtener el último mensaje de cada conversación
                    MensajeDirectoDto existente = conversacionesUnicas.get(mensaje.getConversationId());
                    if (existente == null || 
                        (mensaje.getCreatedAt() != null && existente.getCreatedAt() != null &&
                         mensaje.getCreatedAt().isAfter(existente.getCreatedAt()))) {
                        conversacionesUnicas.put(mensaje.getConversationId(), mensaje);
                    }
                }
            }
            
            long mensajes = conversacionesUnicas.size();
            // Contar conversaciones donde el ÚLTIMO mensaje no está leído
            // Esto coincide con lo que se muestra en la lista del frontend
            long noLeidos = conversacionesUnicas.values().stream()
                    .filter(mensaje -> {
                        if (mensaje == null) return false;
                        // Un mensaje se considera leído si:
                        // - isRead = true
                        // - estado = "respondido"
                        // - senderType = "empleado" (enviado por empleado)
                        boolean esLeido = mensaje.isRead() || 
                                         "respondido".equalsIgnoreCase(mensaje.getEstado()) ||
                                         "empleado".equalsIgnoreCase(mensaje.getSenderType());
                        return !esLeido;
                    })
                    .count();
            
            java.util.Map<String, Object> estadisticas = new java.util.HashMap<>();
            estadisticas.put("mensajes", mensajes);
            estadisticas.put("noLeidos", noLeidos);
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

