package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.technova.technov.domain.dto.DashboardDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.DashboardService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controlador para el Dashboard del administrador.
 */
@Controller
public class AdminDashboardController {

    private final DashboardService dashboardService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        try {
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
                return "redirect:/login";
            }

            DashboardDto dashboard = dashboardService.obtenerDashboard();
            
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("usuario", usuario);
            
            return "frontend/admin/dashboard";
        } catch (Exception e) {
            System.err.println("Error en AdminDashboardController: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, redirigir al perfil o mostrar error
            return "redirect:/admin/perfil";
        }
    }
}

