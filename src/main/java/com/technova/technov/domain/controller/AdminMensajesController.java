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
        
        // Inicializar valores por defecto
        model.addAttribute("todosMensajes", new java.util.ArrayList<>());
        model.addAttribute("conversaciones", new java.util.HashMap<>());
        model.addAttribute("noLeidosPorConversacion", new java.util.HashMap<>());
        model.addAttribute("empleados", new java.util.ArrayList<>());
        model.addAttribute("nombresEmpleados", new java.util.HashMap<>());
        model.addAttribute("mensajesConversacion", new java.util.ArrayList<>());
        model.addAttribute("empleadoConversacion", null);
        model.addAttribute("quejasEmpleados", new java.util.ArrayList<>());
        
        // Cargar mensajes de empleado
        try {
            List<com.technova.technov.domain.dto.MensajeEmpleadoDto> todosMensajes = null;
            try {
                todosMensajes = mensajeEmpleadoService.listarTodos();
            } catch (Exception e) {
                System.err.println("Error al listar todos los mensajes: " + e.getMessage());
                todosMensajes = new java.util.ArrayList<>();
            }
            if (todosMensajes == null) {
                todosMensajes = new java.util.ArrayList<>();
            }
            model.addAttribute("todosMensajes", todosMensajes);
            
            // Agrupar mensajes por empleado para crear conversaciones
            java.util.Map<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>> conversacionesTemp = new java.util.HashMap<>();
            java.util.Map<Long, Integer> noLeidosPorConversacion = new java.util.HashMap<>();
            java.util.Map<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>> conversaciones = new java.util.LinkedHashMap<>();
            
            try {
                conversacionesTemp = todosMensajes.stream()
                        .filter(m -> m != null && m.getEmpleadoId() != null)
                        .collect(java.util.stream.Collectors.groupingBy(m -> m.getEmpleadoId()));
                
                // Calcular mensajes no leídos por conversación y ordenar mensajes dentro de cada conversación
                for (java.util.Map.Entry<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>> entry : conversacionesTemp.entrySet()) {
                    if (entry.getValue() != null) {
                        // Ordenar mensajes dentro de cada conversación: más recientes primero
                        entry.getValue().sort((a, b) -> {
                            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                                return b.getCreatedAt().compareTo(a.getCreatedAt());
                            }
                            if (a.getCreatedAt() == null && b.getCreatedAt() != null) return 1;
                            if (a.getCreatedAt() != null && b.getCreatedAt() == null) return -1;
                            return 0;
                        });
                        
                        long noLeidos = entry.getValue().stream()
                                .filter(m -> m != null && !m.isLeido())
                                .count();
                        noLeidosPorConversacion.put(entry.getKey(), (int)noLeidos);
                    }
                }
                
                // Ordenar conversaciones por el mensaje más reciente (estilo WhatsApp)
                java.util.List<java.util.Map.Entry<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>>> conversacionesOrdenadas = 
                    new java.util.ArrayList<>(conversacionesTemp.entrySet());
                
                conversacionesOrdenadas.sort((entry1, entry2) -> {
                    try {
                        java.time.Instant fecha1 = null;
                        java.time.Instant fecha2 = null;
                        
                        if (entry1.getValue() != null && !entry1.getValue().isEmpty()) {
                            com.technova.technov.domain.dto.MensajeEmpleadoDto ultimoMensaje1 = entry1.getValue().get(0); // Ya está ordenado, el primero es el más reciente
                            if (ultimoMensaje1 != null) {
                                fecha1 = ultimoMensaje1.getCreatedAt();
                            }
                        }
                        
                        if (entry2.getValue() != null && !entry2.getValue().isEmpty()) {
                            com.technova.technov.domain.dto.MensajeEmpleadoDto ultimoMensaje2 = entry2.getValue().get(0); // Ya está ordenado, el primero es el más reciente
                            if (ultimoMensaje2 != null) {
                                fecha2 = ultimoMensaje2.getCreatedAt();
                            }
                        }
                        
                        if (fecha1 == null && fecha2 == null) return 0;
                        if (fecha1 == null) return 1; // Sin fecha va al final
                        if (fecha2 == null) return -1; // Sin fecha va al final
                        
                        // Ordenar descendente: más reciente primero
                        return fecha2.compareTo(fecha1);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                
                // Crear LinkedHashMap ordenado para mantener el orden
                conversaciones = new java.util.LinkedHashMap<>();
                for (java.util.Map.Entry<Long, List<com.technova.technov.domain.dto.MensajeEmpleadoDto>> entry : conversacionesOrdenadas) {
                    conversaciones.put(entry.getKey(), entry.getValue());
                }
                
            } catch (Exception e) {
                System.err.println("Error al agrupar conversaciones: " + e.getMessage());
                e.printStackTrace();
                conversacionesTemp = new java.util.HashMap<>();
                noLeidosPorConversacion = new java.util.HashMap<>();
                conversaciones = new java.util.LinkedHashMap<>();
            }
            
            model.addAttribute("conversaciones", conversaciones);
            model.addAttribute("noLeidosPorConversacion", noLeidosPorConversacion);
            
            // Obtener lista de empleados para el selector
            List<com.technova.technov.domain.dto.UsuarioDto> empleados = new java.util.ArrayList<>();
            try {
                List<com.technova.technov.domain.dto.UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
                if (todosUsuarios != null) {
                    empleados = todosUsuarios.stream()
                            .filter(u -> u != null && "empleado".equalsIgnoreCase(u.getRole()))
                            .collect(java.util.stream.Collectors.toList());
                }
            } catch (Exception e) {
                System.err.println("Error al listar empleados: " + e.getMessage());
                empleados = new java.util.ArrayList<>();
            }
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
                                // Ordenar descendente: más recientes primero
                                return b.getCreatedAt().compareTo(a.getCreatedAt());
                            }
                            if (a.getCreatedAt() == null && b.getCreatedAt() != null) return 1;
                            if (a.getCreatedAt() != null && b.getCreatedAt() == null) return -1;
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
            model.addAttribute("noLeidosPorConversacion", new java.util.HashMap<>());
            model.addAttribute("empleados", new java.util.ArrayList<>());
            model.addAttribute("mensajesConversacion", new java.util.ArrayList<>());
        }
        
        // Cargar reclamos enviados por empleados
        try {
            List<ReclamoDto> reclamosEmpleados = reclamoService.listarQuejasEnviadasPorEmpleados();
            if (reclamosEmpleados == null) {
                reclamosEmpleados = new java.util.ArrayList<>();
            }
            model.addAttribute("reclamosEmpleados", reclamosEmpleados);
        } catch (Exception e) {
            System.err.println("Error al cargar reclamos de empleados: " + e.getMessage());
            e.printStackTrace();
            // Asegurar que siempre haya un valor válido
            model.addAttribute("reclamosEmpleados", new java.util.ArrayList<>());
        }
        
        return "frontend/admin/mensajes";
    }
}

