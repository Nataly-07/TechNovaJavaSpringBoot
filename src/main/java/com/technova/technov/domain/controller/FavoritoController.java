package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.FavoritoDto;
import com.technova.technov.domain.service.FavoritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@CrossOrigin("*")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public ResponseEntity<List<FavoritoDto>> listarTodos() {
        List<FavoritoDto> favoritos = favoritoService.listarTodos();
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<FavoritoDto>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/usuario/{usuarioId}/producto/{productoId}")
    public ResponseEntity<FavoritoDto> agregar(
            @PathVariable Long usuarioId,
            @PathVariable Integer productoId) {
        FavoritoDto agregado = favoritoService.agregar(usuarioId, productoId);
        return ResponseEntity.ok(agregado);
    }

    @PostMapping("/usuario/{usuarioId}/producto/{productoId}/toggle")
    public ResponseEntity<Boolean> toggle(
            @PathVariable Long usuarioId,
            @PathVariable Integer productoId) {
        boolean resultado = favoritoService.toggle(usuarioId, productoId);
        return ResponseEntity.ok(resultado);
    }
}

