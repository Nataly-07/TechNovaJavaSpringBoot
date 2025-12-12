package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.AtencionClienteDto;
import com.technova.technov.domain.dto.ReclamoDto;
import com.technova.technov.domain.service.AtencionClienteService;
import com.technova.technov.domain.service.ReclamoService;
import com.technova.technov.domain.repository.AtencionClienteRepository;
import com.technova.technov.domain.entity.AtencionCliente;
import com.technova.technov.domain.service.MensajeEmpleadoService;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de mensajes/tickets de atención al cliente del administrador.
 */
@Controller
public class AdminMensajesController {

    private final AtencionClienteService atencionClienteService;
    private final AtencionClienteRepository atencionClienteRepository;
    private final MensajeEmpleadoService mensajeEmpleadoService;
    private final UsuarioService usuarioService;
    private final ReclamoService reclamoService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminMensajesController(
            AtencionClienteService atencionClienteService,
            AtencionClienteRepository atencionClienteRepository,
            MensajeEmpleadoService mensajeEmpleadoService,
            UsuarioService usuarioService,
            ReclamoService reclamoService) {
        this.atencionClienteService = atencionClienteService;
        this.atencionClienteRepository = atencionClienteRepository;
        this.mensajeEmpleadoService = mensajeEmpleadoService;
        this.usuarioService = usuarioService;
        this.reclamoService = reclamoService;
    }

    @GetMapping("/admin/mensajes")
    public String listarMensajes(
            @RequestParam(required = false) Long conversacionId,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        
        // Cargar mensajes de empleado
        try {
            List<com.technova.technov.domain.dto.MensajeEmpleadoDto> todosMensajes = mensajeEmpleadoService.listarTodos();
            model.addAttribute("todosMensajes", todosMensajes);
            
            // Agrupar mensajes por empleado para crear conversaciones
            java.util.Map<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>> conversaciones = todosMensajes.stream()
                    .collect(java.util.stream.Collectors.groupingBy(m -> m.getEmpleadoId() != null ? m.getEmpleadoId() : 0L));
            model.addAttribute("conversaciones", conversaciones);
            
            // Obtener lista de empleados para el selector
            List<com.technova.technov.domain.dto.UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
            List<com.technova.technov.domain.dto.UsuarioDto> empleados = todosUsuarios.stream()
                    .filter(u -> "empleado".equalsIgnoreCase(u.getRole()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("empleados", empleados);
            
            // Crear mapa de nombres de empleados
            java.util.Map<Long, String> nombresEmpleados = new java.util.HashMap<>();
            for (com.technova.technov.domain.dto.UsuarioDto emp : empleados) {
                if (emp.getId() != null) {
                    nombresEmpleados.put(emp.getId(), emp.getName() != null ? emp.getName() : "Empleado #" + emp.getId());
                }
            }
            model.addAttribute("nombresEmpleados", nombresEmpleados);
            
            // Si hay un ID de conversación, cargar esos mensajes
            if (conversacionId != null) {
                List<com.technova.technov.domain.dto.MensajeEmpleadoDto> mensajesConversacion = todosMensajes.stream()
                        .filter(m -> {
                            // Incluir mensajes del admin a este empleado
                            if (m.getTipoRemitente() != null && "admin".equalsIgnoreCase(m.getTipoRemitente()) && 
                                m.getEmpleadoId() != null && m.getEmpleadoId().equals(conversacionId)) {
                                return true;
                            }
                            // Incluir mensajes del empleado al admin (empleadoId es el ID del empleado, remitenteId es el ID del admin)
                            if (m.getTipoRemitente() != null && "empleado".equalsIgnoreCase(m.getTipoRemitente()) && 
                                m.getEmpleadoId() != null && m.getEmpleadoId().equals(conversacionId)) {
                                return true;
                            }
                            return false;
                        })
                        .sorted((a, b) -> {
                            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                                return a.getCreatedAt().compareTo(b.getCreatedAt());
                            }
                            return 0;
                        })
                        .collect(java.util.stream.Collectors.toList());
                model.addAttribute("mensajesConversacion", mensajesConversacion);
                model.addAttribute("conversacionId", conversacionId);
                
                // Obtener información del empleado de la conversación
                com.technova.technov.domain.dto.UsuarioDto empleadoConversacion = empleados.stream()
                        .filter(e -> e.getId() != null && e.getId().equals(conversacionId))
                        .findFirst()
                        .orElse(null);
                model.addAttribute("empleadoConversacion", empleadoConversacion);
            } else {
                model.addAttribute("mensajesConversacion", new java.util.ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar mensajes de empleado: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("todosMensajes", new java.util.ArrayList<>());
            model.addAttribute("conversaciones", new java.util.HashMap<>());
            model.addAttribute("empleados", new java.util.ArrayList<>());
            model.addAttribute("mensajesConversacion", new java.util.ArrayList<>());
        }
        
        // Cargar quejas enviadas por empleados
        try {
            List<ReclamoDto> quejasEmpleados = reclamoService.listarQuejasEnviadasPorEmpleados();
            model.addAttribute("quejasEmpleados", quejasEmpleados);
        } catch (Exception e) {
            System.err.println("Error al cargar quejas de empleados: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("quejasEmpleados", new java.util.ArrayList<>());
        }
        
        return "frontend/admin/mensajes";
    }
}

