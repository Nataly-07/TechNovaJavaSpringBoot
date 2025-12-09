package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.PagoDto;
import com.technova.technov.domain.service.PagoService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Controlador para la gestión de pagos del administrador.
 */
@Controller
public class AdminPagosController {

    private final PagoService pagoService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminPagosController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/admin/pagos")
    public String listarPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<PagoDto> pagos = pagoService.listarTodos();
        
        // Filtrar por estado
        if (estado != null && !estado.isEmpty()) {
            final String estadoLower = estado.toLowerCase();
            pagos = pagos.stream()
                    .filter(p -> p.getEstadoPago() != null && 
                               p.getEstadoPago().toLowerCase().contains(estadoLower))
                    .collect(Collectors.toList());
        }

        // Filtrar por rango de fechas
        if (fechaDesde != null && !fechaDesde.isEmpty() && fechaHasta != null && !fechaHasta.isEmpty()) {
            try {
                LocalDate desde = LocalDate.parse(fechaDesde);
                LocalDate hasta = LocalDate.parse(fechaHasta);
                pagos = pagos.stream()
                        .filter(p -> p.getFechaPago() != null && 
                                   !p.getFechaPago().isBefore(desde) && 
                                   !p.getFechaPago().isAfter(hasta))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Si hay error en el parseo de fechas, ignorar el filtro
            }
        }

        // Buscar por número de factura
        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            pagos = pagos.stream()
                    .filter(p -> (p.getNumeroFactura() != null && 
                                p.getNumeroFactura().toLowerCase().contains(busquedaLower)))
                    .collect(Collectors.toList());
        }

        // Calcular estadísticas
        List<PagoDto> todosLosPagos = pagoService.listarTodos();
        long totalPagos = todosLosPagos.size();
        BigDecimal totalMonto = todosLosPagos.stream()
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Pagos confirmados
        long pagosConfirmados = todosLosPagos.stream()
                .filter(p -> p.getEstadoPago() != null && 
                           "CONFIRMADO".equalsIgnoreCase(p.getEstadoPago()))
                .count();
        
        // Pagos del mes actual
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        BigDecimal montoEsteMes = todosLosPagos.stream()
                .filter(p -> p.getFechaPago() != null && 
                           !p.getFechaPago().isBefore(inicioMes) && 
                           !p.getFechaPago().isAfter(finMes))
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pagos", pagos);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("totalMonto", totalMonto);
        model.addAttribute("pagosConfirmados", pagosConfirmados);
        model.addAttribute("montoEsteMes", montoEsteMes);
        
        return "frontend/admin/pagos";
    }
}

