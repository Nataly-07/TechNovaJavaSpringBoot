package com.technova.technov.domain.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.technova.technov.domain.dto.ReclamoDto;
import com.technova.technov.domain.service.ReclamoService;

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin("*")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReclamoDto>> listarPorUsuario(@PathVariable Integer usuarioId) {
        List<ReclamoDto> reclamos = reclamoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(reclamos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReclamoDto>> listarPorEstado(@PathVariable String estado) {
        List<ReclamoDto> reclamos = reclamoService.listarPorEstado(estado);
        return ResponseEntity.ok(reclamos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamoDto> obtenerPorId(@PathVariable Integer id) {
        ReclamoDto reclamo = reclamoService.detalle(id);
        if (reclamo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reclamo);
    }

    @PostMapping
    public ResponseEntity<?> crearReclamo(
            @RequestParam Integer usuarioId,
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam(required = false, defaultValue = "normal") String prioridad) {
        try {
            ReclamoDto creado = reclamoService.crearReclamo(usuarioId, titulo, descripcion, prioridad);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/responder")
    public ResponseEntity<?> responderReclamo(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String respuesta = request.get("respuesta");
            if (respuesta == null || respuesta.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La respuesta no puede estar vacía");
            }
            ReclamoDto reclamoRespondido = reclamoService.responder(id, respuesta);
            if (reclamoRespondido == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(reclamoRespondido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<ReclamoDto> cerrarReclamo(@PathVariable Integer id) {
        ReclamoDto reclamoCerrado = reclamoService.cerrar(id);
        if (reclamoCerrado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reclamoCerrado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReclamo(@PathVariable Integer id) {
        boolean eliminado = reclamoService.eliminar(id);
        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/enviar-al-admin")
    public ResponseEntity<?> enviarAlAdministrador(@PathVariable Integer id) {
        try {
            ReclamoDto reclamoEnviado = reclamoService.enviarAlAdministrador(id);
            if (reclamoEnviado == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(reclamoEnviado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/evaluar-resolucion")
    public ResponseEntity<?> evaluarResolucion(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String evaluacion = request.get("evaluacion");
            if (evaluacion == null || evaluacion.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La evaluación no puede estar vacía");
            }
            ReclamoDto reclamoEvaluado = reclamoService.evaluarResolucion(id, evaluacion);
            if (reclamoEvaluado == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(reclamoEvaluado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}

