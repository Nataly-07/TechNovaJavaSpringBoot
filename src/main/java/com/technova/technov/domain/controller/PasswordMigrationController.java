package com.technova.technov.domain.controller;

import com.technova.technov.service.PasswordMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para migrar contraseñas de texto plano a BCrypt.
 * IMPORTANTE: Este controlador debe eliminarse o protegerse después de la migración.
 */
@Controller
public class PasswordMigrationController {

    @Autowired
    private PasswordMigrationService passwordMigrationService;

    /**
     * Página para ejecutar la migración de contraseñas.
     * Solo debe ser accesible durante la migración inicial.
     */
    @GetMapping("/admin/migrar-contrasenas")
    public String mostrarMigracion(Model model) {
        return "admin/migracion-contrasenas";
    }

    /**
     * Ejecuta la migración de todas las contraseñas.
     */
    @PostMapping("/admin/migrar-contrasenas/todas")
    public String migrarTodas(RedirectAttributes redirectAttributes) {
        try {
            int contador = passwordMigrationService.migrarContrasenas();
            redirectAttributes.addFlashAttribute("mensaje", 
                "Migración completada. Se migraron " + contador + " contraseñas.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", 
                "Error al migrar contraseñas: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/migrar-contrasenas";
    }

    /**
     * Ejecuta la migración de una contraseña específica por email.
     */
    @PostMapping("/admin/migrar-contrasenas/por-email")
    public String migrarPorEmail(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            boolean migrado = passwordMigrationService.migrarContrasenaPorEmail(email);
            if (migrado) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Contraseña migrada exitosamente para: " + email);
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "No se pudo migrar. El usuario no existe o la contraseña ya está codificada.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", 
                "Error al migrar contraseña: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/migrar-contrasenas";
    }
}




