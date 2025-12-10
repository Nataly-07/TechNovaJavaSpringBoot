package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
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

        // Obtener solo usuarios activos para las estadísticas totales
        List<UsuarioDto> todosLosUsuarios = usuarioService.listarUsuarios();
        long totalClientes = todosLosUsuarios.stream()
                .filter(u -> "cliente".equalsIgnoreCase(u.getRole()) && u.getEstado() != null && u.getEstado())
                .count();
        long totalAdmin = todosLosUsuarios.stream()
                .filter(u -> "admin".equalsIgnoreCase(u.getRole()) && u.getEstado() != null && u.getEstado())
                .count();
        long totalEmpleados = todosLosUsuarios.stream()
                .filter(u -> "empleado".equalsIgnoreCase(u.getRole()) && u.getEstado() != null && u.getEstado())
                .count();

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

    @PostMapping("/admin/usuarios/crear")
    public String crearUsuario(
            @ModelAttribute UsuarioDto usuarioDto,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        // Validar que solo se puedan crear administradores o empleados
        String rol = usuarioDto.getRole();
        if (rol == null || (!"admin".equalsIgnoreCase(rol) && !"empleado".equalsIgnoreCase(rol))) {
            redirectAttributes.addFlashAttribute("mensaje", "Solo se pueden crear administradores o empleados desde esta sección");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/usuarios";
        }

        try {
            // Generar nombre completo si no está presente
            if (usuarioDto.getName() == null || usuarioDto.getName().trim().isEmpty()) {
                String nombreCompleto = "";
                if (usuarioDto.getFirstName() != null) {
                    nombreCompleto += usuarioDto.getFirstName();
                }
                if (usuarioDto.getLastName() != null) {
                    nombreCompleto += " " + usuarioDto.getLastName();
                }
                usuarioDto.setName(nombreCompleto.trim());
            }

            UsuarioDto usuarioCreado = usuarioService.crearUsuario(usuarioDto);
            
            if (usuarioCreado != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al crear el usuario");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        } catch (Exception e) {
            String errorMessage = "Error al crear el usuario";
            if (e.getMessage() != null) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("email") || msg.contains("duplicate")) {
                    errorMessage = "El correo electrónico ya está registrado";
                } else if (msg.contains("document") || msg.contains("documento")) {
                    errorMessage = "El número de documento ya está registrado";
                }
            }
            redirectAttributes.addFlashAttribute("mensaje", errorMessage);
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/reactivar")
    public String reactivarUsuarioPorEmail(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            Optional<UsuarioDto> usuarioInactivo = usuarioService.usuarioPorEmail(email);
            if (usuarioInactivo.isPresent()) {
                UsuarioDto u = usuarioInactivo.get();
                boolean resultado = usuarioService.activarDesactivarUsuario(u.getId(), true);
                if (resultado) {
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario " + email + " reactivado correctamente");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                } else {
                    redirectAttributes.addFlashAttribute("mensaje", "Error al reactivar el usuario");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                }
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado: " + email);
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al reactivar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/usuarios";
    }

    /**
     * Página de emergencia para reactivar usuarios desactivados.
     */
    @GetMapping("/admin/usuarios/emergencia")
    public String paginaEmergencia() {
        return "emergencia-reactivar";
    }

    /**
     * Endpoint de emergencia para reactivar usuarios desactivados.
     * IMPORTANTE: Este endpoint debe ser eliminado o protegido después de usar.
     */
    @PostMapping("/admin/usuarios/emergencia/reactivar")
    public String reactivarUsuarioEmergencia(
            @RequestParam String email,
            @RequestParam(required = false) String token,
            RedirectAttributes redirectAttributes) {
        
        // Token simple de emergencia (cambiar por algo más seguro en producción)
        String tokenEsperado = "EMERGENCIA2025";
        
        if (token == null || !tokenEsperado.equals(token)) {
            redirectAttributes.addFlashAttribute("mensaje", "Token de emergencia inválido");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/login";
        }

        try {
            Optional<UsuarioDto> usuarioInactivo = usuarioService.usuarioPorEmail(email);
            if (usuarioInactivo.isPresent()) {
                UsuarioDto u = usuarioInactivo.get();
                boolean resultado = usuarioService.activarDesactivarUsuario(u.getId(), true);
                if (resultado) {
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario " + email + " reactivado correctamente. Por favor, inicia sesión.");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                } else {
                    redirectAttributes.addFlashAttribute("mensaje", "Error al reactivar el usuario");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                }
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado: " + email);
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al reactivar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/login";
    }
}

