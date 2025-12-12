package com.technova.technov.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador MVC para exponer las pantallas de autenticación / registro.
 * Spring Security maneja el procesamiento del login automáticamente.
 */
@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/registro")
    public String registro() {
        return "usuarios/registro";
    }

    @GetMapping("/login")
    public String login(Model model, 
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String accountActivated, 
                       @RequestParam(required = false) String accountDeactivated,
                       HttpServletRequest request) {
        if (accountActivated != null && "true".equals(accountActivated)) {
            model.addAttribute("successMessage", "¡Cuenta activada correctamente! Ya puedes iniciar sesión.");
        }
        if (accountDeactivated != null && "true".equals(accountDeactivated)) {
            model.addAttribute("infoMessage", "Tu cuenta ha sido desactivada correctamente.");
        }
        
        // Verificar si el error es por cuenta desactivada
        if (error != null && "true".equals(error)) {
            String email = request.getParameter("email");
            if (email != null && !email.isEmpty()) {
                // Buscar si existe un usuario con ese email pero inactivo
                usuarioRepository.findByEmail(email)
                    .filter(usuario -> usuario.getEstado() != null && !usuario.getEstado())
                    .ifPresent(usuario -> {
                        model.addAttribute("accountDeactivatedMessage", 
                            "Esta cuenta ha sido desactivada por el usuario. Si deseas reactivarla, puedes contactar con el administrador o usar la opción de recuperación de cuenta.");
                    });
            }
        }
        
        return "usuarios/login";
    }
}

