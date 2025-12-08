package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.VentaService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de inventario y movimientos de artículos del administrador.
 */
@Controller
public class AdminInventarioController {

    private final ProductoService productoService;
    private final ComprasService comprasService;
    private final VentaService ventaService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminInventarioController(
            ProductoService productoService,
            ComprasService comprasService,
            VentaService ventaService) {
        this.productoService = productoService;
        this.comprasService = comprasService;
        this.ventaService = ventaService;
    }

    @GetMapping("/admin/inventario")
    public String listarInventario(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String tipoMovimiento,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<ProductoDto> productos = productoService.listarProductos();
        
        // Aplicar filtros
        if (categoria != null && !categoria.isEmpty()) {
            productos = productoService.porCategoria(categoria);
        }

        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            productos = productos.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(busquedaLower)) ||
                               (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(busquedaLower)))
                    .collect(Collectors.toList());
        }

        // Obtener movimientos recientes
        List<CompraDto> comprasRecientes = comprasService.listar();
        List<VentaDto> ventasRecientes = ventaService.listar();

        // Filtrar movimientos por tipo si se especifica
        if (tipoMovimiento != null && !tipoMovimiento.isEmpty()) {
            if ("entrada".equalsIgnoreCase(tipoMovimiento)) {
                ventasRecientes = List.of(); // Solo mostrar compras
            } else if ("salida".equalsIgnoreCase(tipoMovimiento)) {
                comprasRecientes = List.of(); // Solo mostrar ventas
            }
        }

        // Calcular estadísticas (usando todos los productos, no solo los filtrados)
        List<ProductoDto> todosLosProductos = productoService.listarProductos();
        int totalProductos = todosLosProductos.size();
        long productosBajoStock = todosLosProductos.stream()
                .filter(p -> p.getStock() != null && p.getStock() < 10)
                .count();
        long productosAgotados = todosLosProductos.stream()
                .filter(p -> p.getStock() == null || p.getStock() == 0)
                .count();

        model.addAttribute("productos", productos);
        model.addAttribute("categoria", categoria);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("tipoMovimiento", tipoMovimiento);
        model.addAttribute("usuario", usuario);
        model.addAttribute("comprasRecientes", comprasRecientes);
        model.addAttribute("ventasRecientes", ventasRecientes);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("productosBajoStock", productosBajoStock);
        model.addAttribute("productosAgotados", productosAgotados);
        
        return "frontend/admin/inventario";
    }
}

