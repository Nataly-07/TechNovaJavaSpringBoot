package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String login(Model model, @RequestParam(required = false) String accountActivated, 
                       @RequestParam(required = false) String accountDeactivated) {
        if (accountActivated != null && "true".equals(accountActivated)) {
            model.addAttribute("successMessage", "¡Cuenta activada correctamente! Ya puedes iniciar sesión.");
        }
        if (accountDeactivated != null && "true".equals(accountDeactivated)) {
            model.addAttribute("successMessage", "Cuenta desactivada correctamente. Puedes reactivarla desde el enlace '¿Cuenta desactivada?' en la página de login.");
        }
        return "usuarios/login";
    }
}

