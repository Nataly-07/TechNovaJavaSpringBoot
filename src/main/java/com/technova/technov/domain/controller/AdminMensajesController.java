package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.AtencionClienteDto;
import com.technova.technov.domain.service.AtencionClienteService;
import com.technova.technov.domain.repository.AtencionClienteRepository;
import com.technova.technov.domain.entity.AtencionCliente;

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
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminMensajesController(
            AtencionClienteService atencionClienteService,
            AtencionClienteRepository atencionClienteRepository) {
        this.atencionClienteService = atencionClienteService;
        this.atencionClienteRepository = atencionClienteRepository;
    }

    @GetMapping("/admin/mensajes")
    public String listarMensajes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<AtencionClienteDto> tickets;
        
        // Obtener todos los tickets o filtrar por estado
        if (estado != null && !estado.isEmpty() && !"todos".equalsIgnoreCase(estado)) {
            tickets = atencionClienteService.listarPorEstado(estado);
        } else {
            // Obtener todos los tickets usando el servicio para manejar correctamente la carga lazy
            tickets = atencionClienteService.listarTodos();
        }

        // Aplicar filtro de búsqueda
        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            tickets = tickets.stream()
                    .filter(t -> (t.getTema() != null && t.getTema().toLowerCase().contains(busquedaLower)) ||
                               (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(busquedaLower)))
                    .collect(Collectors.toList());
        }

        // Calcular estadísticas usando el servicio
        List<AtencionClienteDto> todosLosTicketsAbiertos = atencionClienteService.listarPorEstado("abierto");
        List<AtencionClienteDto> todosLosTicketsEnProceso = atencionClienteService.listarPorEstado("en_proceso");
        List<AtencionClienteDto> todosLosTicketsResueltos = atencionClienteService.listarPorEstado("resuelto");
        
        // Obtener todos para el total
        List<AtencionCliente> todosTickets = atencionClienteRepository.findAll();
        
        long totalTickets = todosTickets.size();
        long ticketsAbiertos = todosLosTicketsAbiertos.size();
        long ticketsEnProceso = todosLosTicketsEnProceso.size();
        long ticketsResueltos = todosLosTicketsResueltos.size();
        long ticketsPendientes = ticketsAbiertos + ticketsEnProceso;

        model.addAttribute("tickets", tickets);
        model.addAttribute("estado", estado);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("ticketsAbiertos", ticketsAbiertos);
        model.addAttribute("ticketsEnProceso", ticketsEnProceso);
        model.addAttribute("ticketsResueltos", ticketsResueltos);
        model.addAttribute("ticketsPendientes", ticketsPendientes);
        
        return "frontend/admin/mensajes";
    }
}

