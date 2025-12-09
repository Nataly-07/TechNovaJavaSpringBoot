package com.technova.technov.domain.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.UsuarioService;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.service.ReporteService;
import com.technova.technov.domain.repository.CaracteristicaRepository;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para los reportes del administrador.
 */
@Controller
public class ReporteController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final VentaService ventaService;
    private final ReporteService reporteService;
    private final CaracteristicaRepository caracteristicaRepository;
    
    @Autowired
    private SecurityUtil securityUtil;

    public ReporteController(
            ProductoService productoService,
            UsuarioService usuarioService,
            VentaService ventaService,
            ReporteService reporteService,
            CaracteristicaRepository caracteristicaRepository) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.ventaService = ventaService;
        this.reporteService = reporteService;
        this.caracteristicaRepository = caracteristicaRepository;
    }

    @GetMapping("/admin/reportes")
    public String index(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        // Obtener estadísticas para las tarjetas
        int productosCount = productoService.listarProductos().size();
        int usuariosCount = usuarioService.listarUsuarios().size();
        int ventasCount = ventaService.listar().size();

        // Obtener categorías únicas del repositorio y normalizarlas
        List<String> categorias = caracteristicaRepository.listarCategorias();
        // Normalizar a minúsculas y eliminar duplicados (case-insensitive), filtrando "temporal"
        categorias = categorias.stream()
                .filter(c -> c != null && !c.trim().isEmpty() && !c.equalsIgnoreCase("temporal"))
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Obtener marcas únicas del repositorio y normalizarlas
        List<String> marcas = caracteristicaRepository.listarMarcas();
        // Normalizar y eliminar duplicados (case-insensitive), filtrando "Temporal"
        marcas = marcas.stream()
                .filter(m -> m != null && !m.trim().isEmpty() && !m.equalsIgnoreCase("Temporal"))
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("productosCount", productosCount);
        model.addAttribute("usuariosCount", usuariosCount);
        model.addAttribute("ventasCount", ventasCount);
        model.addAttribute("categorias", categorias);
        model.addAttribute("marcas", marcas);
        
        return "frontend/admin/reportes/index";
    }

    @GetMapping("/admin/reportes/preview-productos")
    public String previewProductos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<ProductoDto> productos = productoService.listarProductos();
        
        // Aplicar filtros
        if (categoria != null && !categoria.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getCategoria() != null &&
                            p.getCaracteristica().getCategoria().toLowerCase().contains(categoria.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (marca != null && !marca.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getMarca() != null &&
                            p.getCaracteristica().getMarca().toLowerCase().contains(marca.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (precioMin != null) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getPrecioVenta() != null &&
                            p.getCaracteristica().getPrecioVenta().doubleValue() >= precioMin)
                    .collect(Collectors.toList());
        }

        if (precioMax != null) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getPrecioVenta() != null &&
                            p.getCaracteristica().getPrecioVenta().doubleValue() <= precioMax)
                    .collect(Collectors.toList());
        }

        // Limitar a 50 para vista previa
        productos = productos.stream().limit(50).collect(Collectors.toList());

        model.addAttribute("productos", productos);
        model.addAttribute("categoria", categoria);
        model.addAttribute("marca", marca);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);

        return "frontend/admin/reportes/preview-productos";
    }

    @GetMapping("/admin/reportes/preview-usuarios")
    public String previewUsuarios(
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

        // Limitar a 50 para vista previa
        usuarios = usuarios.stream().limit(50).collect(Collectors.toList());

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rol", rol);
        model.addAttribute("busqueda", busqueda);

        return "frontend/admin/reportes/preview-usuarios";
    }

    @GetMapping("/admin/reportes/preview-ventas")
    public String previewVentas(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<VentaDto> ventas = ventaService.listar();
        
        // Limitar a 50 para vista previa
        ventas = ventas.stream().limit(50).collect(Collectors.toList());

        model.addAttribute("ventas", ventas);
        model.addAttribute("categoria", categoria);
        model.addAttribute("marca", marca);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "frontend/admin/reportes/preview-ventas";
    }

    @GetMapping("/admin/reportes/productos/pdf")
    public ResponseEntity<ByteArrayResource> productosPdf(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<ProductoDto> productos = obtenerProductosFiltrados(categoria, marca, precioMin, precioMax);
        byte[] pdfBytes = reporteService.generarPdfProductos(productos);

        String filename = "reporte_productos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfBytes));
    }

    private List<ProductoDto> obtenerProductosFiltrados(String categoria, String marca, Double precioMin, Double precioMax) {
        List<ProductoDto> productos = productoService.listarProductos();
        
        if (categoria != null && !categoria.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getCategoria() != null &&
                            p.getCaracteristica().getCategoria().toLowerCase().contains(categoria.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (marca != null && !marca.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getMarca() != null &&
                            p.getCaracteristica().getMarca().toLowerCase().contains(marca.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (precioMin != null) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getPrecioVenta() != null &&
                            p.getCaracteristica().getPrecioVenta().doubleValue() >= precioMin)
                    .collect(Collectors.toList());
        }

        if (precioMax != null) {
            productos = productos.stream()
                    .filter(p -> p.getCaracteristica() != null && 
                            p.getCaracteristica().getPrecioVenta() != null &&
                            p.getCaracteristica().getPrecioVenta().doubleValue() <= precioMax)
                    .collect(Collectors.toList());
        }

        return productos;
    }

    @GetMapping("/admin/reportes/usuarios/pdf")
    public ResponseEntity<ByteArrayResource> usuariosPdf(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String busqueda) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<UsuarioDto> usuarios = usuarioService.listarUsuarios();
        
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

        byte[] pdfBytes = reporteService.generarPdfUsuarios(usuarios);

        String filename = "reporte_usuarios_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfBytes));
    }

    @GetMapping("/admin/reportes/ventas/pdf")
    public ResponseEntity<ByteArrayResource> ventasPdf(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String estado) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<VentaDto> ventas = ventaService.listar();
        
        // Los filtros de categoría y marca requerirían unirse con productos, 
        // por simplicidad solo aplicamos filtro de estado si existe
        if (estado != null && !estado.isEmpty()) {
            // Nota: VentaDto no tiene campo estado, así que se omite este filtro
            // Se puede implementar después si se agrega el campo estado a VentaDto
        }

        byte[] pdfBytes = reporteService.generarPdfVentas(ventas);

        String filename = "reporte_ventas_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfBytes));
    }

    @GetMapping("/admin/reportes/productos/excel")
    public ResponseEntity<ByteArrayResource> productosExcel(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<ProductoDto> productos = obtenerProductosFiltrados(categoria, marca, precioMin, precioMax);
        byte[] excelBytes = reporteService.generarExcelProductos(productos);

        String filename = "reporte_productos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(excelBytes));
    }

    @GetMapping("/admin/reportes/usuarios/excel")
    public ResponseEntity<ByteArrayResource> usuariosExcel(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String busqueda) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<UsuarioDto> usuarios = usuarioService.listarUsuarios();
        
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

        byte[] excelBytes = reporteService.generarExcelUsuarios(usuarios);

        String filename = "reporte_usuarios_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(excelBytes));
    }

    @GetMapping("/admin/reportes/ventas/excel")
    public ResponseEntity<ByteArrayResource> ventasExcel(
            @RequestParam(required = false) String estado) throws IOException {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity.status(403).build();
        }

        List<VentaDto> ventas = ventaService.listar();
        
        // Nota: VentaDto no tiene campo estado, así que se omite este filtro
        // Se puede implementar después si se agrega el campo estado a VentaDto

        byte[] excelBytes = reporteService.generarExcelVentas(ventas);

        String filename = "reporte_ventas_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(excelBytes));
    }
}
