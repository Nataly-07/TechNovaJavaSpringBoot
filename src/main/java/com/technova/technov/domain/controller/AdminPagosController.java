package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.PagoDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.service.PagoService;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Controlador para la gestión de pagos del administrador.
 */
@Controller
public class AdminPagosController {

    private final PagoService pagoService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private VentaService ventaService;
    
    @Autowired
    private UsuarioService usuarioService;

    public AdminPagosController(PagoService pagoService) {
        this.pagoService = pagoService;
    }
    
    /**
     * Extrae el ID de venta del número de factura.
     * Soporta múltiples formatos:
     * - FACT-{Año}-{ID_Venta}: FACT-2025-000123 -> 123
     * - Números simples que coincidan con IDs de venta
     */
    private Integer extraerVentaIdDeFactura(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isEmpty()) {
            System.out.println("  DEBUG extraerVentaId: número de factura es null o vacío");
            return null;
        }
        try {
            System.out.println("  DEBUG extraerVentaId: procesando factura: " + numeroFactura);
            // Buscar el patrón FACT-{año}-{id}
            Pattern pattern = Pattern.compile("FACT-\\d+-(\\d+)");
            Matcher matcher = pattern.matcher(numeroFactura);
            if (matcher.find()) {
                String idStr = matcher.group(1);
                System.out.println("  DEBUG extraerVentaId: ID extraído (con ceros): " + idStr);
                // Eliminar ceros a la izquierda - Integer.parseInt lo hace automáticamente
                Integer ventaId = Integer.parseInt(idStr);
                System.out.println("  DEBUG extraerVentaId: ID parseado: " + ventaId);
                return ventaId;
            }
            
            System.out.println("  DEBUG extraerVentaId: no coincide con patrón FACT-{año}-{id}");
            
            // Si no coincide con el patrón FACT, intentar como número directo
            // Solo si es un número simple (sin letras ni guiones)
            if (numeroFactura.matches("^\\d+$")) {
                try {
                    Integer posibleId = Integer.parseInt(numeroFactura);
                    System.out.println("  DEBUG extraerVentaId: intentando como número directo: " + posibleId);
                    // Verificar que existe una venta con ese ID
                    VentaDto venta = ventaService.detalle(posibleId);
                    if (venta != null) {
                        System.out.println("  DEBUG extraerVentaId: venta encontrada con número directo");
                        return posibleId;
                    }
                } catch (Exception e) {
                    System.out.println("  DEBUG extraerVentaId: error al verificar número directo: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al extraer ID de venta de factura: " + numeroFactura + " - " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("  DEBUG extraerVentaId: no se pudo extraer ID de venta");
        return null;
    }

    @GetMapping("/admin/pagos")
    public String listarPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false, defaultValue = "reciente") String orden,
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

        // Obtener información de ventas y usuarios asociados a cada pago (antes de filtrar por búsqueda)
        Map<Integer, VentaDto> ventasPorPagoTemp = new HashMap<>();
        Map<Integer, String> nombresClientesTemp = new HashMap<>();
        Map<Integer, String> emailsClientesTemp = new HashMap<>();
        
        System.out.println("=== DEBUG: Procesando " + pagos.size() + " pagos ===");
        
        // Primero, intentar vincular pagos con ventas usando el número de factura
        for (PagoDto pago : pagos) {
            try {
                System.out.println("Procesando pago ID: " + pago.getId() + ", Factura: " + pago.getNumeroFactura());
                Integer ventaId = extraerVentaIdDeFactura(pago.getNumeroFactura());
                System.out.println("  -> Venta ID extraído: " + ventaId);
                
                if (ventaId != null) {
                    try {
                        VentaDto venta = ventaService.detalle(ventaId);
                        if (venta != null) {
                            System.out.println("  -> Venta encontrada: " + venta.getVentaId());
                            ventasPorPagoTemp.put(pago.getId(), venta);
                            
                            // Obtener información del cliente
                            if (venta.getUsuarioId() != null) {
                                try {
                                    java.util.Optional<UsuarioDto> clienteOpt = usuarioService.usuarioPorId(Long.valueOf(venta.getUsuarioId()));
                                    if (clienteOpt.isPresent()) {
                                        UsuarioDto cliente = clienteOpt.get();
                                        String nombreCompleto = (cliente.getName() != null ? cliente.getName() : "N/A");
                                        nombresClientesTemp.put(pago.getId(), nombreCompleto);
                                        emailsClientesTemp.put(pago.getId(), cliente.getEmail() != null ? cliente.getEmail() : "N/A");
                                        System.out.println("  -> Cliente: " + nombreCompleto);
                                    } else {
                                        nombresClientesTemp.put(pago.getId(), "Cliente ID: " + venta.getUsuarioId());
                                        emailsClientesTemp.put(pago.getId(), "N/A");
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al obtener usuario para venta " + ventaId + ": " + e.getMessage());
                                    e.printStackTrace();
                                    nombresClientesTemp.put(pago.getId(), "Cliente ID: " + venta.getUsuarioId());
                                    emailsClientesTemp.put(pago.getId(), "N/A");
                                }
                            }
                        } else {
                            System.out.println("  -> Venta NO encontrada para ID: " + ventaId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener venta " + ventaId + " para pago " + pago.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("  -> No se pudo extraer ID de venta de factura: " + pago.getNumeroFactura());
                }
            } catch (Exception e) {
                System.err.println("Error al procesar pago " + pago.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=== DEBUG: Pagos con ventas asociadas: " + ventasPorPagoTemp.size() + " ===");
        
        // Si un pago no tiene venta asociada pero tiene un número de factura que parece un ID de venta,
        // intentar buscar directamente por ese número
        for (PagoDto pago : pagos) {
            if (!ventasPorPagoTemp.containsKey(pago.getId()) && pago.getNumeroFactura() != null) {
                try {
                    // Intentar interpretar el número de factura como ID de venta directo
                    if (pago.getNumeroFactura().matches("^\\d+$")) {
                        Integer posibleVentaId = Integer.parseInt(pago.getNumeroFactura());
                        VentaDto venta = ventaService.detalle(posibleVentaId);
                        if (venta != null) {
                            ventasPorPagoTemp.put(pago.getId(), venta);
                            if (venta.getUsuarioId() != null) {
                                try {
                                    java.util.Optional<UsuarioDto> clienteOpt = usuarioService.usuarioPorId(Long.valueOf(venta.getUsuarioId()));
                                    if (clienteOpt.isPresent()) {
                                        UsuarioDto cliente = clienteOpt.get();
                                        nombresClientesTemp.put(pago.getId(), cliente.getName() != null ? cliente.getName() : "N/A");
                                        emailsClientesTemp.put(pago.getId(), cliente.getEmail() != null ? cliente.getEmail() : "N/A");
                                    }
                                } catch (Exception e) {
                                    // Ignorar errores al obtener cliente
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores al intentar vincular
                }
            }
        }
        
        // Buscar por número de factura, nombre de cliente o email
        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            pagos = pagos.stream()
                    .filter(p -> {
                        // Buscar en número de factura
                        boolean matchFactura = p.getNumeroFactura() != null && 
                                            p.getNumeroFactura().toLowerCase().contains(busquedaLower);
                        
                        // Buscar en nombre de cliente
                        boolean matchNombre = nombresClientesTemp.containsKey(p.getId()) && 
                                            nombresClientesTemp.get(p.getId()) != null &&
                                            nombresClientesTemp.get(p.getId()).toLowerCase().contains(busquedaLower);
                        
                        // Buscar en email de cliente
                        boolean matchEmail = emailsClientesTemp.containsKey(p.getId()) && 
                                           emailsClientesTemp.get(p.getId()) != null &&
                                           emailsClientesTemp.get(p.getId()).toLowerCase().contains(busquedaLower);
                        
                        return matchFactura || matchNombre || matchEmail;
                    })
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

        // Filtrar solo pagos que tienen ventas asociadas
        List<PagoDto> pagosConVenta = new ArrayList<>();
        Map<Integer, VentaDto> ventasPorPago = new HashMap<>();
        Map<Integer, String> nombresClientes = new HashMap<>();
        Map<Integer, String> emailsClientes = new HashMap<>();
        
        for (PagoDto pago : pagos) {
            if (ventasPorPagoTemp.containsKey(pago.getId())) {
                pagosConVenta.add(pago);
                ventasPorPago.put(pago.getId(), ventasPorPagoTemp.get(pago.getId()));
                if (nombresClientesTemp.containsKey(pago.getId())) {
                    nombresClientes.put(pago.getId(), nombresClientesTemp.get(pago.getId()));
                }
                if (emailsClientesTemp.containsKey(pago.getId())) {
                    emailsClientes.put(pago.getId(), emailsClientesTemp.get(pago.getId()));
                }
            }
        }
        
        // Ordenar pagos según el filtro seleccionado
        if ("antiguo".equalsIgnoreCase(orden)) {
            // Más antiguo primero
            pagosConVenta = pagosConVenta.stream()
                    .sorted((p1, p2) -> {
                        if (p1.getFechaPago() == null && p2.getFechaPago() == null) return 0;
                        if (p1.getFechaPago() == null) return 1;
                        if (p2.getFechaPago() == null) return -1;
                        int fechaCompare = p1.getFechaPago().compareTo(p2.getFechaPago());
                        if (fechaCompare != 0) return fechaCompare;
                        // Si las fechas son iguales, ordenar por ID ascendente
                        if (p1.getId() == null && p2.getId() == null) return 0;
                        if (p1.getId() == null) return 1;
                        if (p2.getId() == null) return -1;
                        return p1.getId().compareTo(p2.getId());
                    })
                    .collect(Collectors.toList());
        } else {
            // Más reciente primero (por defecto)
            pagosConVenta = pagosConVenta.stream()
                    .sorted((p1, p2) -> {
                        if (p1.getFechaPago() == null && p2.getFechaPago() == null) return 0;
                        if (p1.getFechaPago() == null) return 1;
                        if (p2.getFechaPago() == null) return -1;
                        int fechaCompare = p2.getFechaPago().compareTo(p1.getFechaPago());
                        if (fechaCompare != 0) return fechaCompare;
                        // Si las fechas son iguales, ordenar por ID descendente
                        if (p1.getId() == null && p2.getId() == null) return 0;
                        if (p1.getId() == null) return 1;
                        if (p2.getId() == null) return -1;
                        return p2.getId().compareTo(p1.getId());
                    })
                    .collect(Collectors.toList());
        }
        
        pagos = pagosConVenta;
        
        model.addAttribute("pagos", pagos);
        model.addAttribute("ventasPorPago", ventasPorPago);
        model.addAttribute("nombresClientes", nombresClientes);
        model.addAttribute("emailsClientes", emailsClientes);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("orden", orden);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("totalMonto", totalMonto);
        model.addAttribute("pagosConfirmados", pagosConfirmados);
        model.addAttribute("montoEsteMes", montoEsteMes);
        
        return "frontend/admin/pagos";
    }
    
    @GetMapping("/admin/pagos/detalle/{pagoId}")
    public String verDetallePago(@PathVariable Integer pagoId, Model model) {
        try {
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
                return "redirect:/login";
            }
            
            // Obtener el pago
            List<PagoDto> todosPagos = pagoService.listarTodos();
            PagoDto pago = todosPagos.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(pagoId))
                    .findFirst()
                    .orElse(null);
            
            if (pago == null) {
                System.err.println("ERROR: Pago no encontrado con ID: " + pagoId);
                return "redirect:/admin/pagos?error=pago_no_encontrado";
            }
            
            System.out.println("=== DEBUG verDetallePago ===");
            System.out.println("  -> Pago ID: " + pago.getId());
            System.out.println("  -> Número Factura: " + pago.getNumeroFactura());
            
            // Obtener la venta asociada
            Integer ventaId = null;
            try {
                ventaId = extraerVentaIdDeFactura(pago.getNumeroFactura());
                System.out.println("  -> Venta ID extraído: " + ventaId);
            } catch (Exception e) {
                System.err.println("ERROR al extraer ID de venta: " + e.getMessage());
                e.printStackTrace();
            }
            
            VentaDto venta = null;
            UsuarioDto cliente = null;
            
            if (ventaId != null) {
                try {
                    venta = ventaService.detalle(ventaId);
                    System.out.println("  -> Venta obtenida: " + (venta != null ? "Sí" : "No"));
                    if (venta != null) {
                        System.out.println("  -> Venta ID: " + venta.getVentaId());
                        System.out.println("  -> Usuario ID en venta: " + venta.getUsuarioId());
                        
                        if (venta.getUsuarioId() != null) {
                            try {
                                java.util.Optional<UsuarioDto> clienteOpt = usuarioService.usuarioPorId(Long.valueOf(venta.getUsuarioId()));
                                if (clienteOpt.isPresent()) {
                                    cliente = clienteOpt.get();
                                    System.out.println("  -> Cliente obtenido: " + (cliente.getName() != null ? cliente.getName() : "N/A"));
                                } else {
                                    System.err.println("  -> Cliente no encontrado para usuario ID: " + venta.getUsuarioId());
                                }
                            } catch (Exception e) {
                                System.err.println("ERROR al obtener cliente: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("ERROR al obtener venta: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("  -> No se pudo extraer ID de venta de factura: " + pago.getNumeroFactura());
            }
            
            if (venta == null) {
                System.err.println("ERROR: Venta no encontrada para pago ID: " + pagoId);
                return "redirect:/admin/pagos?error=venta_no_encontrada";
            }
            
            // Asegurar que todos los atributos estén presentes
            model.addAttribute("pago", pago);
            model.addAttribute("venta", venta);
            model.addAttribute("cliente", cliente);
            model.addAttribute("usuario", usuario);
            
            System.out.println("  -> Redirigiendo a vista de detalle");
            return "frontend/admin/pago-detalle";
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO en verDetallePago: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/pagos?error=error_interno";
        }
    }
    
    @GetMapping("/admin/pagos/factura/{pagoId}")
    public String generarFactura(@PathVariable Integer pagoId, Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener el pago
        List<PagoDto> todosPagos = pagoService.listarTodos();
        PagoDto pago = todosPagos.stream()
                .filter(p -> p.getId() != null && p.getId().equals(pagoId))
                .findFirst()
                .orElse(null);
        
        if (pago == null) {
            return "redirect:/admin/pagos?error=pago_no_encontrado";
        }
        
        // Obtener la venta asociada
        Integer ventaId = extraerVentaIdDeFactura(pago.getNumeroFactura());
        VentaDto venta = null;
        UsuarioDto cliente = null;
        
        if (ventaId != null) {
            try {
                venta = ventaService.detalle(ventaId);
                if (venta != null && venta.getUsuarioId() != null) {
                    java.util.Optional<UsuarioDto> clienteOpt = usuarioService.usuarioPorId(Long.valueOf(venta.getUsuarioId()));
                    if (clienteOpt.isPresent()) {
                        cliente = clienteOpt.get();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al obtener venta para factura " + pagoId + ": " + e.getMessage());
            }
        }
        
        if (venta == null) {
            return "redirect:/admin/pagos?error=venta_no_encontrada";
        }
        
        model.addAttribute("pago", pago);
        model.addAttribute("venta", venta);
        model.addAttribute("cliente", cliente);
        model.addAttribute("usuario", usuario);
        
        // Retornar vista de factura (por ahora HTML, luego se puede convertir a PDF)
        return "frontend/admin/factura";
    }
}

