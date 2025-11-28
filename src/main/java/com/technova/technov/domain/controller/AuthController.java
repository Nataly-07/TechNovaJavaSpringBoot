package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC para exponer las pantallas de autenticación / registro.
 * Spring Security maneja el procesamiento del login automáticamente.
 */
@Controller
public class AuthController {

    @GetMapping("/registro")
    public String registro() {
        return "usuarios/registro";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "usuarios/login";
    }
}

