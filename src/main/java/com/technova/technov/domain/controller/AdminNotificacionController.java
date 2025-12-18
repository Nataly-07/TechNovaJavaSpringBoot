package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.NotificacionDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.NotificacionService;
import com.technova.technov.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminNotificacionController {

    private final NotificacionService notificacionService;
    private final SecurityUtil securityUtil;

    public AdminNotificacionController(NotificacionService notificacionService, SecurityUtil securityUtil) {
        this.notificacionService = notificacionService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/admin/notificaciones")
    public String listarNotificaciones(
            @RequestParam(required = false) String modulo,
            Model model) {

        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);

        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        // Listar todas las notificaciones del sistema
        List<NotificacionDto> notificaciones = notificacionService.listarTodos();

        // Filtrar por módulo si se especifica
        if (modulo != null && !modulo.isEmpty()) {
            final String mod = modulo;
            notificaciones = notificaciones.stream()
                    .filter(n -> mod.equalsIgnoreCase(n.getTipo()))
                    .collect(Collectors.toList());
            model.addAttribute("filtroModulo", modulo);
        } else {
            model.addAttribute("filtroModulo", "todos");
        }

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("usuario", usuario);

        return "frontend/admin/notificaciones/lista";
    }

    @PostMapping("/admin/notificaciones/marcar-leida/{id}")
    public String marcarLeida(@PathVariable Long id) {
        notificacionService.marcarLeida(id);
        return "redirect:/admin/notificaciones";
    }
}
