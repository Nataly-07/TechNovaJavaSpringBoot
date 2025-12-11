package com.technova.technov.domain.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para manejar las solicitudes de favicon.ico
 * y evitar errores 404 en la consola del navegador.
 */
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // Retornar 204 No Content para evitar el error 404
        // El navegador buscará el favicon en el link tag del HTML
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

