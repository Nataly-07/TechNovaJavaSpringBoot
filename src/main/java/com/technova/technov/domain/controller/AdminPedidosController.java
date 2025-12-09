package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Controlador para la gestión de pedidos/ventas del administrador.
 */
@Controller
public class AdminPedidosController {

    private final VentaService ventaService;
    private final UsuarioService usuarioService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminPedidosController(
            VentaService ventaService,
            UsuarioService usuarioService) {
        this.ventaService = ventaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/admin/pedidos")
    public String listarPedidos(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<VentaDto> pedidos = ventaService.listar();
        
        // Aplicar filtros
        if (usuarioId != null) {
            pedidos = ventaService.porUsuario(usuarioId);
        }

        if (fechaDesde != null && !fechaDesde.isEmpty() && fechaHasta != null && !fechaHasta.isEmpty()) {
            try {
                LocalDate desde = LocalDate.parse(fechaDesde);
                LocalDate hasta = LocalDate.parse(fechaHasta);
                pedidos = pedidos.stream()
                        .filter(p -> p.getFechaVenta() != null && 
                                   !p.getFechaVenta().isBefore(desde) && 
                                   !p.getFechaVenta().isAfter(hasta))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Si hay error en el parseo de fechas, ignorar el filtro
            }
        }

        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            pedidos = pedidos.stream()
                    .filter(p -> {
                        // Buscar en items del pedido
                        if (p.getItems() != null) {
                            return p.getItems().stream()
                                    .anyMatch(item -> item.getNombreProducto() != null && 
                                                   item.getNombreProducto().toLowerCase().contains(busquedaLower));
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // Calcular estadísticas
        List<VentaDto> todosLosPedidos = ventaService.listar();
        long totalPedidos = todosLosPedidos.size();
        BigDecimal totalVentas = todosLosPedidos.stream()
                .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Pedidos del mes actual
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        long pedidosEsteMes = todosLosPedidos.stream()
                .filter(p -> p.getFechaVenta() != null && 
                           !p.getFechaVenta().isBefore(inicioMes) && 
                           !p.getFechaVenta().isAfter(finMes))
                .count();

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("pedidosEsteMes", pedidosEsteMes);
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        
        return "frontend/admin/pedidos";
    }
}

