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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.AtencionClienteDto;
import com.technova.technov.domain.service.AtencionClienteService;

@RestController
@RequestMapping("/api/atencion-cliente")
@CrossOrigin("*")
public class AtencionClienteController {

    private final AtencionClienteService atencionClienteService;

    public AtencionClienteController(AtencionClienteService atencionClienteService) {
        this.atencionClienteService = atencionClienteService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AtencionClienteDto>> listarPorUsuario(@PathVariable Integer usuarioId) {
        List<AtencionClienteDto> tickets = atencionClienteService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AtencionClienteDto>> listarPorEstado(@PathVariable String estado) {
        List<AtencionClienteDto> tickets = atencionClienteService.listarPorEstado(estado);
        return ResponseEntity.ok(tickets);
    }

  
    @GetMapping("/{id}")
    public ResponseEntity<AtencionClienteDto> obtenerPorId(@PathVariable Integer id) {
        AtencionClienteDto ticket = atencionClienteService.detalle(id);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }

    @PostMapping
    public ResponseEntity<?> crearTicket(
            @RequestParam Integer usuarioId,
            @RequestParam String tema,
            @RequestParam String descripcion) {
        try {
            AtencionClienteDto creado = atencionClienteService.crearTicket(usuarioId, tema, descripcion);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<AtencionClienteDto> actualizarTicket(@PathVariable Integer id, @RequestBody AtencionClienteDto atencionClienteDto) {
        AtencionClienteDto ticketActualizado = atencionClienteService.actualizar(id, atencionClienteDto);
        if (ticketActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticketActualizado);
    }

    
    @PutMapping("/{id}/responder")
    public ResponseEntity<?> responderTicket(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String respuesta = request.get("respuesta");
            if (respuesta == null || respuesta.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La respuesta no puede estar vacía");
            }
            AtencionClienteDto ticketRespondido = atencionClienteService.responder(id, respuesta);
            if (ticketRespondido == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ticketRespondido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<AtencionClienteDto> cerrarTicket(@PathVariable Integer id) {
        AtencionClienteDto ticketCerrado = atencionClienteService.cerrar(id);
        if (ticketCerrado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticketCerrado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Integer id) {
        boolean eliminarTicket = atencionClienteService.eliminar(id);
        if (!eliminarTicket) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

