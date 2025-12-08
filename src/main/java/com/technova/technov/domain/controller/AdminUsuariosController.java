package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de usuarios del administrador.
 */
@Controller
public class AdminUsuariosController {

    private final UsuarioService usuarioService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminUsuariosController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/admin/usuarios")
    public String listarUsuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<UsuarioDto> usuarios = usuarioService.listarUsuarios();
        
        // Aplicar filtros
        if (rol != null && !rol.isEmpty()) {
            usuarios = usuarios.stream()
                    .filter(u -> rol.equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
        }

        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            usuarios = usuarios.stream()
                    .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(busquedaLower)) ||
                               (u.getEmail() != null && u.getEmail().toLowerCase().contains(busquedaLower)) ||
                               (u.getDocumentNumber() != null && u.getDocumentNumber().contains(busqueda)))
                    .collect(Collectors.toList());
        }

        // Obtener todos los usuarios para las estadísticas totales
        List<UsuarioDto> todosLosUsuarios = usuarioService.listarUsuarios();
        long totalClientes = todosLosUsuarios.stream().filter(u -> "cliente".equalsIgnoreCase(u.getRole())).count();
        long totalAdmin = todosLosUsuarios.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long totalEmpleados = todosLosUsuarios.stream().filter(u -> "empleado".equalsIgnoreCase(u.getRole())).count();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rol", rol);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalUsuarios", todosLosUsuarios.size());
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalAdmin", totalAdmin);
        model.addAttribute("totalEmpleados", totalEmpleados);
        
        return "frontend/admin/usuarios";
    }
}

