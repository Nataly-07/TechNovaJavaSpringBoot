package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.domain.service.CaracteristicaService;
import com.technova.technov.domain.service.ProveedorService;
import com.technova.technov.domain.dto.ProveedorDto;
import com.technova.technov.domain.repository.CaracteristicaRepository;
import com.technova.technov.domain.repository.ProductoRepository;

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
    private final CaracteristicaService caracteristicaService;
    private final ProveedorService proveedorService;
    private final CaracteristicaRepository caracteristicaRepository;
    private final ProductoRepository productoRepository;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminInventarioController(
            ProductoService productoService,
            ComprasService comprasService,
            VentaService ventaService,
            CaracteristicaService caracteristicaService,
            ProveedorService proveedorService,
            CaracteristicaRepository caracteristicaRepository,
            ProductoRepository productoRepository) {
        this.productoService = productoService;
        this.comprasService = comprasService;
        this.ventaService = ventaService;
        this.caracteristicaService = caracteristicaService;
        this.proveedorService = proveedorService;
        this.caracteristicaRepository = caracteristicaRepository;
        this.productoRepository = productoRepository;
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
        // Normalizar y eliminar duplicados (case-insensitive)
        marcas = marcas.stream()
                .filter(m -> m != null && !m.trim().isEmpty() && !m.equalsIgnoreCase("Temporal"))
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Obtener solo proveedores registrados en la página de proveedores
        List<ProveedorDto> proveedores = proveedorService.listarProveedores();

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
        model.addAttribute("marcas", marcas);
        model.addAttribute("categorias", categorias);
        model.addAttribute("proveedores", proveedores);
        
        // Obtener información detallada de categorías y marcas con conteo de productos
        // Usar try-catch para evitar que errores en esta sección rompan toda la página
        List<CategoriaInfo> categoriasInfo = List.of();
        List<MarcaInfo> marcasInfo = List.of();
        
        try {
            categoriasInfo = obtenerCategoriasConInfo();
        } catch (Exception e) {
            // Log del error pero continuar con lista vacía
            categoriasInfo = List.of();
        }
        
        try {
            marcasInfo = obtenerMarcasConInfo();
        } catch (Exception e) {
            // Log del error pero continuar con lista vacía
            marcasInfo = List.of();
        }
        
        model.addAttribute("categoriasInfo", categoriasInfo);
        model.addAttribute("marcasInfo", marcasInfo);
        
        return "frontend/admin/inventario";
    }
    
    // Clase interna para información de categoría
    public static class CategoriaInfo {
        private String nombre;
        private long cantidadProductos;
        private boolean esTemporal;
        
        public CategoriaInfo(String nombre, long cantidadProductos, boolean esTemporal) {
            this.nombre = nombre;
            this.cantidadProductos = cantidadProductos;
            this.esTemporal = esTemporal;
        }
        
        public String getNombre() { return nombre; }
        public long getCantidadProductos() { return cantidadProductos; }
        public boolean isEsTemporal() { return esTemporal; }
    }
    
    // Clase interna para información de marca
    public static class MarcaInfo {
        private String nombre;
        private long cantidadProductos;
        private boolean esTemporal;
        
        public MarcaInfo(String nombre, long cantidadProductos, boolean esTemporal) {
            this.nombre = nombre;
            this.cantidadProductos = cantidadProductos;
            this.esTemporal = esTemporal;
        }
        
        public String getNombre() { return nombre; }
        public long getCantidadProductos() { return cantidadProductos; }
        public boolean isEsTemporal() { return esTemporal; }
    }
    
    private List<CategoriaInfo> obtenerCategoriasConInfo() {
        try {
            List<String> categorias = caracteristicaRepository.listarCategorias();
            if (categorias == null || categorias.isEmpty()) {
                return List.of();
            }
            return categorias.stream()
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .map(categoria -> {
                        try {
                            List<com.technova.technov.domain.entity.Producto> productos = 
                                productoRepository.findByCaracteristica_CategoriaIgnoreCaseAndEstadoTrue(categoria);
                            long cantidad = productos != null ? productos.size() : 0;
                            boolean esTemporal = categoria.equalsIgnoreCase("temporal");
                            return new CategoriaInfo(categoria, cantidad, esTemporal);
                        } catch (Exception e) {
                            // Si hay error al obtener productos, retornar con cantidad 0
                            return new CategoriaInfo(categoria, 0, categoria.equalsIgnoreCase("temporal"));
                        }
                    })
                    .filter(c -> !c.isEsTemporal()) // Filtrar categorías temporales
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Si hay error, retornar lista vacía para evitar que falle la página
            return List.of();
        }
    }
    
    private List<MarcaInfo> obtenerMarcasConInfo() {
        try {
            List<String> marcas = caracteristicaRepository.listarMarcas();
            if (marcas == null || marcas.isEmpty()) {
                return List.of();
            }
            return marcas.stream()
                    .filter(m -> m != null && !m.trim().isEmpty() && !m.equalsIgnoreCase("Temporal"))
                    .map(String::trim)
                    .distinct()
                    .sorted()
                    .map(marca -> {
                        try {
                            List<com.technova.technov.domain.entity.Producto> productos = 
                                productoRepository.findByCaracteristica_MarcaIgnoreCaseAndEstadoTrue(marca);
                            long cantidad = productos != null ? productos.size() : 0;
                            return new MarcaInfo(marca, cantidad, false);
                        } catch (Exception e) {
                            // Si hay error al obtener productos, retornar con cantidad 0
                            return new MarcaInfo(marca, 0, false);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Si hay error, retornar lista vacía para evitar que falle la página
            return List.of();
        }
    }

    @PostMapping("/admin/inventario/crear")
    public String crearProducto(
            @RequestParam String codigo,
            @RequestParam String nombre,
            @RequestParam Integer stock,
            @RequestParam String categoria,
            @RequestParam String marca,
            @RequestParam String color,
            @RequestParam(required = false) String descripcion,
            @RequestParam java.math.BigDecimal precioCompra,
            @RequestParam java.math.BigDecimal precioVenta,
            @RequestParam(required = false) String proveedor,
            @RequestParam(required = false) String imagen,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            // Crear características primero
            CaracteristicasDto caracteristicaDto = new CaracteristicasDto();
            caracteristicaDto.setCategoria(categoria);
            caracteristicaDto.setMarca(marca);
            caracteristicaDto.setColor(color);
            caracteristicaDto.setDescripcion(descripcion != null ? descripcion : "");
            caracteristicaDto.setPrecioCompra(precioCompra);
            caracteristicaDto.setPrecioVenta(precioVenta);
            
            CaracteristicasDto caracteristicaCreada = caracteristicaService.crear(caracteristicaDto);
            
            // Crear producto con referencia a las características
            ProductoDto productoDto = new ProductoDto();
            productoDto.setCodigo(codigo);
            productoDto.setNombre(nombre);
            productoDto.setStock(stock);
            productoDto.setCaracteristicasId(caracteristicaCreada.getId());
            productoDto.setProveedor(proveedor != null ? proveedor : "");
            productoDto.setImagen(imagen != null ? imagen : "");
            
            ProductoDto productoCreado = productoService.crearProducto(productoDto);
            
            if (productoCreado != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Producto creado correctamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al crear el producto");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        } catch (Exception e) {
            String errorMessage = "Error al crear el producto";
            if (e.getMessage() != null) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("duplicate") || msg.contains("duplicado")) {
                    errorMessage = "El código del producto ya existe";
                } else {
                    errorMessage = e.getMessage();
                }
            }
            redirectAttributes.addFlashAttribute("mensaje", errorMessage);
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/inventario/crear-categoria")
    public String crearCategoria(
            @RequestParam String nombreCategoria,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            // Normalizar el nombre de la categoría
            String categoriaNormalizada = nombreCategoria.trim().toLowerCase();
            
            if (categoriaNormalizada.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "El nombre de la categoría no puede estar vacío");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Verificar si la categoría ya existe (case-insensitive)
            List<String> categoriasExistentes = caracteristicaRepository.listarCategorias();
            if (categoriasExistentes.stream().anyMatch(c -> c != null && c.toLowerCase().equals(categoriaNormalizada))) {
                redirectAttributes.addFlashAttribute("mensaje", "La categoría ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Crear una característica "plantilla" solo para agregar la categoría a la lista
            // Usamos valores por defecto que no se usarán hasta crear un producto real
            CaracteristicasDto caracteristicaDto = new CaracteristicasDto();
            caracteristicaDto.setCategoria(categoriaNormalizada);
            caracteristicaDto.setMarca("Temporal"); // Marca temporal
            caracteristicaDto.setColor("N/A"); // Color temporal
            caracteristicaDto.setDescripcion("Categoría creada: " + nombreCategoria);
            caracteristicaDto.setPrecioCompra(java.math.BigDecimal.ZERO);
            caracteristicaDto.setPrecioVenta(java.math.BigDecimal.ZERO);
            
            caracteristicaService.crear(caracteristicaDto);
            
            redirectAttributes.addFlashAttribute("mensaje", "Categoría creada correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear la categoría: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/inventario/crear-marca")
    public String crearMarca(
            @RequestParam String nombreMarca,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            // Normalizar el nombre de la marca
            String marcaNormalizada = nombreMarca.trim();
            
            if (marcaNormalizada.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "El nombre de la marca no puede estar vacío");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Verificar si la marca ya existe (case-insensitive)
            List<String> marcasExistentes = caracteristicaRepository.listarMarcas();
            if (marcasExistentes.stream().anyMatch(m -> m != null && m.trim().equalsIgnoreCase(marcaNormalizada))) {
                redirectAttributes.addFlashAttribute("mensaje", "La marca ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Crear una característica "plantilla" solo para agregar la marca a la lista
            // Usamos valores por defecto que no se usarán hasta crear un producto real
            CaracteristicasDto caracteristicaDto = new CaracteristicasDto();
            caracteristicaDto.setCategoria("temporal"); // Categoría temporal (en minúsculas)
            caracteristicaDto.setMarca(marcaNormalizada);
            caracteristicaDto.setColor("N/A"); // Color temporal
            caracteristicaDto.setDescripcion("Marca creada: " + nombreMarca);
            caracteristicaDto.setPrecioCompra(java.math.BigDecimal.ZERO);
            caracteristicaDto.setPrecioVenta(java.math.BigDecimal.ZERO);
            
            caracteristicaService.crear(caracteristicaDto);
            
            redirectAttributes.addFlashAttribute("mensaje", "Marca creada correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear la marca: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/inventario/eliminar-categoria")
    public String eliminarCategoria(
            @RequestParam String nombreCategoria,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            String categoriaNormalizada = nombreCategoria.trim().toLowerCase();
            
            // Verificar si hay productos asociados
            List<com.technova.technov.domain.entity.Producto> productos = 
                productoRepository.findByCaracteristica_CategoriaIgnoreCaseAndEstadoTrue(categoriaNormalizada);
            
            if (!productos.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "No se puede eliminar la categoría porque tiene " + productos.size() + " producto(s) asociado(s)");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Eliminar todas las características con esta categoría (soft delete)
            List<com.technova.technov.domain.entity.Caracteristica> caracteristicas = 
                caracteristicaRepository.findByCategoriaIgnoreCase(categoriaNormalizada);
            
            for (com.technova.technov.domain.entity.Caracteristica c : caracteristicas) {
                c.setEstado(false);
                caracteristicaRepository.save(c);
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar la categoría: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/inventario/eliminar-marca")
    public String eliminarMarca(
            @RequestParam String nombreMarca,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            String marcaNormalizada = nombreMarca.trim();
            
            // Verificar si hay productos asociados
            List<com.technova.technov.domain.entity.Producto> productos = 
                productoRepository.findByCaracteristica_MarcaIgnoreCaseAndEstadoTrue(marcaNormalizada);
            
            if (!productos.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "No se puede eliminar la marca porque tiene " + productos.size() + " producto(s) asociado(s)");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/admin/inventario";
            }

            // Eliminar todas las características con esta marca (soft delete)
            List<com.technova.technov.domain.entity.Caracteristica> caracteristicas = 
                caracteristicaRepository.findByMarcaIgnoreCase(marcaNormalizada);
            
            for (com.technova.technov.domain.entity.Caracteristica c : caracteristicas) {
                c.setEstado(false);
                caracteristicaRepository.save(c);
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Marca eliminada correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar la marca: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return "redirect:/admin/inventario";
    }
}

