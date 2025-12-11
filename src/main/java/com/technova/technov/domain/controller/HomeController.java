package com.technova.technov.domain.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.dto.CheckoutResponseDto;
import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.FavoritoDto;
import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.service.CaracteristicaService;
import com.technova.technov.domain.service.CarritoService;
import com.technova.technov.domain.service.CheckoutService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.FavoritoService;
import com.technova.technov.domain.service.MedioDePagoService;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.service.ReporteService;

/**
 * Controlador para las vistas públicas del frontend (landing page, categorías, marcas, etc.).
 */
@Controller
public class HomeController {

    private final ProductoService productoService;
    private final CaracteristicaService caracteristicaService;
    private final CarritoService carritoService;
    private final FavoritoService favoritoService;
    private final ComprasService comprasService;
    private final MedioDePagoService medioDePagoService;
    private final VentaService ventaService;
    private final CheckoutService checkoutService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private ReporteService reporteService;

    public HomeController(
            ProductoService productoService,
            CaracteristicaService caracteristicaService,
            CarritoService carritoService,
            FavoritoService favoritoService,
            ComprasService comprasService,
            MedioDePagoService medioDePagoService,
            VentaService ventaService,
            CheckoutService checkoutService) {
        this.productoService = productoService;
        this.caracteristicaService = caracteristicaService;
        this.carritoService = carritoService;
        this.favoritoService = favoritoService;
        this.comprasService = comprasService;
        this.medioDePagoService = medioDePagoService;
        this.ventaService = ventaService;
        this.checkoutService = checkoutService;
    }

    /**
     * Método auxiliar para enriquecer productos con características y calcular precios
     */
    private List<ProductoDto> enriquecerProductos(List<ProductoDto> productos) {
        return productos.stream().map(producto -> {
            if (producto.getCaracteristicasId() != null) {
                CaracteristicasDto caracteristica = caracteristicaService.caracteristicaPorId(producto.getCaracteristicasId()).orElse(null);
                producto.setCaracteristica(caracteristica);
                
                if (caracteristica != null && caracteristica.getPrecioVenta() != null) {
                    BigDecimal precioVenta = caracteristica.getPrecioVenta();
                    BigDecimal precioOriginal = precioVenta.multiply(new BigDecimal("1.05")).setScale(0, RoundingMode.HALF_UP);
                    producto.setPrecioOriginal(precioOriginal.doubleValue());
                    producto.setPrecioDescuento(precioVenta.doubleValue());
                }
            }
            return producto;
        }).collect(Collectors.toList());
    }

    @GetMapping({"/", "/inicio"})
    public String index(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.listarProductos();
        model.addAttribute("productos", enriquecerProductos(productos));
        
        // Cargar categorías y marcas dinámicamente desde la base de datos
        List<String> categorias = caracteristicaService.listarCategorias();
        List<String> marcas = caracteristicaService.listarMarcas();
        // Filtrar la categoría "temporal"
        categorias = categorias.stream()
                .filter(cat -> !cat.equalsIgnoreCase("temporal"))
                .collect(Collectors.toList());
        model.addAttribute("categorias", categorias);
        model.addAttribute("marcas", marcas);
        
        // Obtener productos recientes para todos los usuarios
        List<ProductoDto> productosRecientes = productoService.listarProductosRecientes(6);
        model.addAttribute("productosRecientes", enriquecerProductos(productosRecientes));
        
        // Si hay un usuario autenticado y es cliente, mostrar index autenticado
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            // Obtener datos adicionales para el cliente autenticado
            int carritoCount = 0;
            int favoritosCount = 0;
            
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    carritoCount = itemsCarrito != null ? itemsCarrito.size() : 0;
                } catch (Exception e) {
                    // Si hay error, simplemente dejar el contador en 0
                    carritoCount = 0;
                }
                
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    favoritosCount = favoritos != null ? favoritos.size() : 0;
                } catch (Exception e) {
                    // Si hay error, simplemente dejar el contador en 0
                    favoritosCount = 0;
                }
            }
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("carritoCount", carritoCount);
            model.addAttribute("favoritosCount", favoritosCount);
            return "frontend/index-autenticado";
        }
        
        // Si no hay usuario o no es cliente, mostrar index normal
        return "index";
    }

    // ========== CATEGORÍAS ==========
    
    @GetMapping("/categoria/{categoria}")
    public String categoria(@PathVariable String categoria, Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        // Capitalizar la categoría para la búsqueda (ej: "celulares" -> "Celulares")
        String categoriaCapitalizada = categoria.substring(0, 1).toUpperCase() + categoria.substring(1).toLowerCase();
        List<ProductoDto> productos = productoService.porCategoria(categoriaCapitalizada);
        model.addAttribute("categoria", categoria);
        model.addAttribute("productos", enriquecerProductos(productos));
        
        // Cargar categorías y marcas dinámicamente desde la base de datos
        List<String> categorias = caracteristicaService.listarCategorias();
        List<String> marcas = caracteristicaService.listarMarcas();
        // Filtrar la categoría "temporal"
        categorias = categorias.stream()
                .filter(cat -> !cat.equalsIgnoreCase("temporal"))
                .collect(Collectors.toList());
        model.addAttribute("categorias", categorias);
        model.addAttribute("marcas", marcas);
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            // Obtener contadores para el cliente
            int carritoCount = 0;
            int favoritosCount = 0;
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    carritoCount = itemsCarrito != null ? itemsCarrito.size() : 0;
                } catch (Exception e) {
                    carritoCount = 0;
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    favoritosCount = favoritos != null ? favoritos.size() : 0;
                } catch (Exception e) {
                    favoritosCount = 0;
                }
            }
            model.addAttribute("carritoCount", carritoCount);
            model.addAttribute("favoritosCount", favoritosCount);
            return "frontend/categoria/categoria-autenticado";
        }
        return "frontend/categoria/categoria";
    }

    @GetMapping("/categoria/celulares")
    public String celulares(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porCategoria("Celulares");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            // Obtener contadores para el cliente
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/categoria/celulares-autenticado";
        }
        return "frontend/categoria/celulares";
    }

    @GetMapping("/categoria/portatiles")
    public String portatiles(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porCategoria("Portátiles");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            // Obtener contadores para el cliente
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/categoria/portatiles-autenticado";
        }
        return "frontend/categoria/portatiles";
    }

    // ========== MARCAS ==========
    
    @GetMapping("/marca/{marca}")
    public String marca(@PathVariable String marca, Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        String marcaCapitalizada = marca.substring(0, 1).toUpperCase() + marca.substring(1).toLowerCase();
        List<ProductoDto> productos = productoService.porMarca(marcaCapitalizada);
        model.addAttribute("productos", enriquecerProductos(productos));
        model.addAttribute("marca", marca);
        
        // Cargar categorías y marcas dinámicamente desde la base de datos
        List<String> categorias = caracteristicaService.listarCategorias();
        List<String> marcas = caracteristicaService.listarMarcas();
        // Filtrar la categoría "temporal"
        categorias = categorias.stream()
                .filter(cat -> !cat.equalsIgnoreCase("temporal"))
                .collect(Collectors.toList());
        model.addAttribute("categorias", categorias);
        model.addAttribute("marcas", marcas);
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            // Obtener contadores para el cliente
            int carritoCount = 0;
            int favoritosCount = 0;
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    carritoCount = itemsCarrito != null ? itemsCarrito.size() : 0;
                } catch (Exception e) {
                    carritoCount = 0;
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    favoritosCount = favoritos != null ? favoritos.size() : 0;
                } catch (Exception e) {
                    favoritosCount = 0;
                }
            }
            model.addAttribute("carritoCount", carritoCount);
            model.addAttribute("favoritosCount", favoritosCount);
            return "frontend/marca/marca-autenticado";
        }
        return "frontend/marca/marca";
    }

    @GetMapping("/marca/apple")
    public String marcaApple(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("Apple");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/apple-autenticado";
        }
        return "frontend/marca/apple";
    }

    @GetMapping("/marca/samsung")
    public String marcaSamsung(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("Samsung");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/samsung-autenticado";
        }
        return "frontend/marca/samsung";
    }

    @GetMapping("/marca/motorola")
    public String marcaMotorola(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("Motorola");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/motorola-autenticado";
        }
        return "frontend/marca/motorola";
    }

    @GetMapping("/marca/xiaomi")
    public String marcaXiaomi(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("Xiaomi");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/xiaomi-autenticado";
        }
        return "frontend/marca/xiaomi";
    }

    @GetMapping("/marca/oppo")
    public String marcaOppo(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("OPPO");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/oppo-autenticado";
        }
        return "frontend/marca/oppo";
    }

    @GetMapping("/marca/lenovo")
    public String marcaLenovo(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        List<ProductoDto> productos = productoService.porMarca("Lenovo");
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
            return "frontend/marca/lenovo-autenticado";
        }
        return "frontend/marca/lenovo";
    }

    // ========== PRODUCTO ==========
    
    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable Integer id, Model model) {
        ProductoDto producto = productoService.productoPorId(id).orElse(null);
        if (producto == null) {
            return "redirect:/";
        }
        
        List<ProductoDto> productoList = List.of(producto);
        List<ProductoDto> productosEnriquecidos = enriquecerProductos(productoList);
        model.addAttribute("producto", productosEnriquecidos.get(0));
        
        // Si hay un usuario autenticado, agregar información adicional
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
            if (usuario.getId() != null) {
                try {
                    List<CarritoItemDto> itemsCarrito = carritoService.listar(usuario.getId().intValue());
                    model.addAttribute("carritoCount", itemsCarrito != null ? itemsCarrito.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("carritoCount", 0);
                }
                try {
                    List<FavoritoDto> favoritos = favoritoService.listarPorUsuario(usuario.getId());
                    model.addAttribute("favoritosCount", favoritos != null ? favoritos.size() : 0);
                } catch (Exception e) {
                    model.addAttribute("favoritosCount", 0);
                }
            }
        }
        
        return "frontend/producto/detalle-producto";
    }

    // ========== OFERTAS ==========
    
    @GetMapping("/ofertas")
    public String ofertas(Model model) {
        List<ProductoDto> productos = productoService.listarProductos();
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/ofertas/ofertas";
    }

    // ========== CARRITO ==========
    
    @GetMapping("/carrito")
    public String carrito(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<CarritoItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> precios = new HashMap<>();
        
        try {
            if (usuario.getId() != null) {
                items = carritoService.listar(usuario.getId().intValue());
                // Calcular total del carrito y obtener precios
                for (CarritoItemDto item : items) {
                    // Obtener precio del producto
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        precios.put(item.getProductoId(), precio);
                        BigDecimal subtotal = precio.multiply(new BigDecimal(item.getCantidad()));
                        total = total.add(subtotal);
                    }
                }
            }
        } catch (Exception e) {
            // Si hay error, simplemente dejar items vacío
            items = new ArrayList<>();
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        model.addAttribute("usuario", usuario);
        return "frontend/carrito/carrito";
    }
    
    @PostMapping("/carrito/agregar/{productoId}")
    public String agregarAlCarrito(@PathVariable Integer productoId) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || usuario.getId() == null) {
            return "redirect:/login";
        }
        
        try {
            carritoService.agregar(usuario.getId().intValue(), productoId, 1);
            return "redirect:/carrito?success=agregado";
        } catch (Exception e) {
            return "redirect:/carrito?error=agregar";
        }
    }

    // ========== FAVORITOS ==========
    
    @GetMapping("/favoritos")
    public String favoritos(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<FavoritoDto> favoritos = new ArrayList<>();
        
        List<ProductoDto> productosFavoritos = new ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                favoritos = favoritoService.listarPorUsuario(usuario.getId());
                // Obtener productos completos para cada favorito
                productosFavoritos = favoritos.stream()
                        .map(fav -> productoService.productoPorId(fav.getProductoId()).orElse(null))
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Si hay error, simplemente dejar favoritos vacío
            favoritos = new ArrayList<>();
            productosFavoritos = new ArrayList<>();
        }
        
        model.addAttribute("favoritos", favoritos);
        model.addAttribute("productos", enriquecerProductos(productosFavoritos));
        model.addAttribute("usuario", usuario);
        return "frontend/favoritos/favoritos";
    }

    // ========== CHECKOUT ==========
    
    @GetMapping("/checkout/informacion")
    public String checkoutInformacion(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "informacion");
        model.addAttribute("usuario", usuario);
        
        List<CarritoItemDto> items = new ArrayList<>();
        Map<Integer, BigDecimal> precios = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (usuario != null && usuario.getId() != null) {
            try {
                items = carritoService.listar(usuario.getId().intValue());
                for (CarritoItemDto item : items) {
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        precios.put(item.getProductoId(), precio);
                        total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                    }
                }
                
                // Si hay productos en el carrito, limpiar errores previos relacionados con carrito vacío
                if (items != null && !items.isEmpty()) {
                    String errorActual = (String) session.getAttribute("error");
                    if (errorActual != null && errorActual.contains("carrito") && errorActual.contains("vacío")) {
                        session.removeAttribute("error");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar productos del carrito en checkout/informacion: " + e.getMessage());
                items = new ArrayList<>();
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/informacion")
    public String procesarInformacion(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentNumber,
            jakarta.servlet.http.HttpSession session) {
        // Guardar datos en sesión
        Map<String, String> informacion = new HashMap<>();
        if (firstName != null) informacion.put("firstName", firstName);
        if (lastName != null) informacion.put("lastName", lastName);
        if (email != null) informacion.put("email", email);
        if (phone != null) informacion.put("phone", phone);
        if (documentType != null) informacion.put("documentType", documentType);
        if (documentNumber != null) informacion.put("documentNumber", documentNumber);
        session.setAttribute("checkout_informacion", informacion);
        
        return "redirect:/checkout/direccion";
    }

    @GetMapping("/checkout/direccion")
    public String checkoutDireccion(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "direccion");
        
        List<CarritoItemDto> items = new ArrayList<>();
        Map<Integer, BigDecimal> precios = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (usuario != null && usuario.getId() != null) {
            try {
                items = carritoService.listar(usuario.getId().intValue());
                for (CarritoItemDto item : items) {
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        precios.put(item.getProductoId(), precio);
                        total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                    }
                }
                
                // Si hay productos en el carrito, limpiar errores previos relacionados con carrito vacío
                if (items != null && !items.isEmpty()) {
                    String errorActual = (String) session.getAttribute("error");
                    if (errorActual != null && errorActual.contains("carrito") && errorActual.contains("vacío")) {
                        session.removeAttribute("error");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar productos del carrito en checkout/direccion: " + e.getMessage());
                items = new ArrayList<>();
            }
        }
        
        // Cargar datos de sesión si existen
        Map<String, String> direccion = (Map<String, String>) session.getAttribute("checkout_direccion");
        if (direccion != null) {
            model.addAttribute("direccion", direccion);
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/direccion")
    public String procesarDireccion(
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) String barrio,
            jakarta.servlet.http.HttpSession session) {
        // Guardar datos en sesión (solo si no están vacíos)
        Map<String, String> direccionData = new HashMap<>();
        if (departamento != null && !departamento.trim().isEmpty()) {
            direccionData.put("departamento", departamento.trim());
        }
        if (ciudad != null && !ciudad.trim().isEmpty()) {
            direccionData.put("ciudad", ciudad.trim());
        }
        if (direccion != null && !direccion.trim().isEmpty()) {
            direccionData.put("direccion", direccion.trim());
        }
        if (localidad != null && !localidad.trim().isEmpty()) {
            direccionData.put("localidad", localidad.trim());
        }
        if (barrio != null && !barrio.trim().isEmpty()) {
            direccionData.put("barrio", barrio.trim());
        }
        session.setAttribute("checkout_direccion", direccionData);
        
        System.out.println("Checkout Dirección guardado: " + direccionData);
        
        return "redirect:/checkout/envio";
    }

    @GetMapping("/checkout/envio")
    public String checkoutEnvio(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "envio");
        
        List<CarritoItemDto> items = new ArrayList<>();
        Map<Integer, BigDecimal> precios = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (usuario != null && usuario.getId() != null) {
            try {
                items = carritoService.listar(usuario.getId().intValue());
                for (CarritoItemDto item : items) {
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        precios.put(item.getProductoId(), precio);
                        total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                    }
                }
                
                // Si hay productos en el carrito, limpiar errores previos relacionados con carrito vacío
                if (items != null && !items.isEmpty()) {
                    String errorActual = (String) session.getAttribute("error");
                    if (errorActual != null && errorActual.contains("carrito") && errorActual.contains("vacío")) {
                        session.removeAttribute("error");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar productos del carrito en checkout/envio: " + e.getMessage());
                items = new ArrayList<>();
            }
        }
        
        // Cargar datos de sesión si existen
        Map<String, String> direccion = (Map<String, String>) session.getAttribute("checkout_direccion");
        if (direccion != null) {
            model.addAttribute("direccion", direccion);
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/envio")
    public String procesarEnvio(
            @RequestParam(required = false) String transportadora,
            @RequestParam(required = false) String fechaEnvio,
            jakarta.servlet.http.HttpSession session) {
        // Guardar datos en sesión (solo si no están vacíos)
        Map<String, String> envio = new HashMap<>();
        if (transportadora != null && !transportadora.trim().isEmpty()) {
            envio.put("transportadora", transportadora.trim());
        }
        if (fechaEnvio != null && !fechaEnvio.trim().isEmpty()) {
            envio.put("fechaEnvio", fechaEnvio.trim());
        }
        session.setAttribute("checkout_envio", envio);
        
        System.out.println("Checkout Envío guardado: " + envio);
        
        return "redirect:/checkout/pago";
    }

    @GetMapping("/checkout/pago")
    public String checkoutPago(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "pago");
        
        List<CarritoItemDto> items = new ArrayList<>();
        Map<Integer, BigDecimal> precios = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (usuario != null && usuario.getId() != null) {
            try {
                items = carritoService.listar(usuario.getId().intValue());
                for (CarritoItemDto item : items) {
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        precios.put(item.getProductoId(), precio);
                        total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                    }
                }
                
                // Si hay productos en el carrito, limpiar errores previos relacionados con carrito vacío
                if (items != null && !items.isEmpty()) {
                    String errorActual = (String) session.getAttribute("error");
                    if (errorActual != null && errorActual.contains("carrito") && errorActual.contains("vacío")) {
                        session.removeAttribute("error");
                    }
                } else {
                    // Si el carrito está vacío, mostrar error
                    session.setAttribute("error", "Tu carrito está vacío. Agrega productos antes de continuar.");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar productos del carrito en checkout/pago: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("error", "Error al cargar los productos del carrito. Por favor, intenta nuevamente.");
                items = new ArrayList<>();
            }
        }
        
        // Métodos de pago fijos
        List<Map<String, String>> metodosFormato = new ArrayList<>();
        Map<String, String> tarjetaCredito = new HashMap<>();
        tarjetaCredito.put("value", "tarjeta_credito");
        tarjetaCredito.put("label", "Tarjeta de Crédito");
        metodosFormato.add(tarjetaCredito);
        
        Map<String, String> tarjetaDebito = new HashMap<>();
        tarjetaDebito.put("value", "tarjeta_debito");
        tarjetaDebito.put("label", "Tarjeta Débito");
        metodosFormato.add(tarjetaDebito);
        
        Map<String, String> nequi = new HashMap<>();
        nequi.put("value", "nequi");
        nequi.put("label", "Nequi");
        metodosFormato.add(nequi);
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        model.addAttribute("metodosDisponibles", metodosFormato);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/pago")
    public String procesarPago(
            @RequestParam(required = false) String metodoPago,
            @RequestParam Map<String, String> datosPago,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Guardar datos en sesión (solo si no están vacíos)
        Map<String, Object> pago = new HashMap<>();
        if (metodoPago != null && !metodoPago.trim().isEmpty()) {
            pago.put("metodoPago", metodoPago.trim());
        }
        // Filtrar datosPago para quitar valores vacíos y el propio metodoPago
        Map<String, String> datosPagoFiltrados = new HashMap<>();
        for (Map.Entry<String, String> entry : datosPago.entrySet()) {
            if (!entry.getKey().equals("metodoPago") && entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                datosPagoFiltrados.put(entry.getKey(), entry.getValue().trim());
            }
        }
        if (!datosPagoFiltrados.isEmpty()) {
            pago.put("datosPago", datosPagoFiltrados);
        }
        session.setAttribute("checkout_pago", pago);
        
        System.out.println("Checkout Pago guardado: " + pago);
        
        return "redirect:/checkout/revision";
    }

    @GetMapping("/checkout/revision")
    public String checkoutRevision(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        model.addAttribute("step", "revision");
        model.addAttribute("usuario", usuario);
        
        // Verificar si hay una compra exitosa para mostrar el modal
        Boolean checkoutSuccess = (Boolean) session.getAttribute("checkout_success");
        if (checkoutSuccess != null && checkoutSuccess) {
            Integer ventaId = (Integer) session.getAttribute("checkout_venta_id");
            
            // Obtener la venta completa desde la base de datos para asegurar datos reales y actualizados
            if (ventaId != null) {
                VentaDto ventaCompleta = null;
                try {
                    ventaCompleta = ventaService.detalle(ventaId);
                } catch (Exception e) {
                    System.err.println("Error al obtener detalles de la venta en checkoutRevision: " + e.getMessage());
                    e.printStackTrace();
                }
                
                if (ventaCompleta != null) {
                    // Calcular el total REAL sumando los precios de los items
                    BigDecimal totalReal = BigDecimal.ZERO;
                    if (ventaCompleta.getItems() != null && !ventaCompleta.getItems().isEmpty()) {
                        for (var item : ventaCompleta.getItems()) {
                            if (item.getPrecioLinea() != null) {
                                totalReal = totalReal.add(item.getPrecioLinea());
                            }
                        }
                    }
                    
                    // Usar el total de la venta si existe y es mayor a 0, de lo contrario usar el calculado
                    BigDecimal totalFinal = (ventaCompleta.getTotal() != null && ventaCompleta.getTotal().compareTo(BigDecimal.ZERO) > 0) 
                                          ? ventaCompleta.getTotal() 
                                          : totalReal;
                    
                    System.out.println("Checkout Revision - Total calculado: $" + totalFinal + 
                                     " (venta.getTotal=" + ventaCompleta.getTotal() + 
                                     ", calculado desde items=" + totalReal + ")");
                    
                    // Pasar todos los datos REALES de la venta al modelo
                    model.addAttribute("showSuccessModal", true);
                    model.addAttribute("ventaId", ventaCompleta.getVentaId());
                    model.addAttribute("total", totalFinal); // Usar el total calculado
                    model.addAttribute("totalReal", totalFinal); // También como totalReal para el template
                    model.addAttribute("fechaVenta", ventaCompleta.getFechaVenta());
                    model.addAttribute("itemsVenta", ventaCompleta.getItems());
                    model.addAttribute("itemsCount", ventaCompleta.getItems() != null ? ventaCompleta.getItems().size() : 0);
                    
                    // Obtener método de pago de la sesión si está disponible
                    String metodoPago = (String) session.getAttribute("checkout_metodo_pago");
                    if (metodoPago != null) {
                        String pagoResumen = metodoPago.replace("_", " ").toUpperCase();
                        model.addAttribute("metodoPagoResumen", pagoResumen);
                    }
                    
                    System.out.println("Checkout Revision - Mostrando modal con datos REALES: Venta #" + ventaCompleta.getVentaId() + 
                                     ", Total Final: $" + totalFinal + 
                                     ", Items: " + (ventaCompleta.getItems() != null ? ventaCompleta.getItems().size() : 0));
                } else {
                    // Si no se puede obtener la venta completa, usar los datos básicos de la sesión
                    System.err.println("Advertencia: No se pudo obtener la venta #" + ventaId + " desde la base de datos, usando datos de sesión");
                    BigDecimal totalSesion = (BigDecimal) session.getAttribute("checkout_total");
                    Integer itemsCountSesion = (Integer) session.getAttribute("checkout_items_count");
                    java.time.LocalDate fechaSesion = (java.time.LocalDate) session.getAttribute("checkout_fecha");
                    
                    model.addAttribute("showSuccessModal", true);
                    model.addAttribute("ventaId", ventaId);
                    model.addAttribute("total", totalSesion != null ? totalSesion : BigDecimal.ZERO);
                    model.addAttribute("totalReal", totalSesion != null ? totalSesion : BigDecimal.ZERO);
                    model.addAttribute("fechaVenta", fechaSesion != null ? fechaSesion : java.time.LocalDate.now());
                    model.addAttribute("itemsVenta", new ArrayList<>());
                    model.addAttribute("itemsCount", itemsCountSesion != null ? itemsCountSesion : 0);
                    
                    String metodoPago = (String) session.getAttribute("checkout_metodo_pago");
                    if (metodoPago != null) {
                        String pagoResumen = metodoPago.replace("_", " ").toUpperCase();
                        model.addAttribute("metodoPagoResumen", pagoResumen);
                    }
                }
            } else {
                model.addAttribute("showSuccessModal", false);
            }
        } else {
            // Asegurar que showSuccessModal sea false cuando no hay compra exitosa
            model.addAttribute("showSuccessModal", false);
        }
        
        // Cargar datos de sesión
        Map<String, String> informacion = (Map<String, String>) session.getAttribute("checkout_informacion");
        Map<String, String> direccion = (Map<String, String>) session.getAttribute("checkout_direccion");
        Map<String, String> envio = (Map<String, String>) session.getAttribute("checkout_envio");
        Map<String, Object> pago = (Map<String, Object>) session.getAttribute("checkout_pago");
        
        // Verificar si los Maps están vacíos (no null pero sin datos válidos)
        boolean direccionValida = direccion != null && !direccion.isEmpty();
        boolean envioValido = envio != null && !envio.isEmpty();
        boolean pagoValido = pago != null && !pago.isEmpty() && pago.containsKey("metodoPago") && pago.get("metodoPago") != null;
        
        // Logs de depuración detallados
        System.out.println("=== Checkout Revision - Estado de Sesión ===");
        System.out.println("Dirección: " + direccion + " (válida: " + direccionValida + ")");
        System.out.println("Envío: " + envio + " (válido: " + envioValido + ")");
        System.out.println("Pago: " + pago + " (válido: " + pagoValido + ")");
        if (pago != null) {
            System.out.println("  - metodoPago: " + pago.get("metodoPago"));
        }
        
        // Agregar al modelo: solo pasar valores válidos (no vacíos) o null
        model.addAttribute("informacion", informacion);
        model.addAttribute("direccion", direccionValida ? direccion : null);
        model.addAttribute("envio", envioValido ? envio : null);
        model.addAttribute("pago", pagoValido ? pago : null);
        
        // Agregar pago resumen si existe
        if (pagoValido) {
            String metodoPago = (String) pago.get("metodoPago");
            if (metodoPago != null && !metodoPago.trim().isEmpty()) {
                String pagoResumen = metodoPago.replace("_", " ").toUpperCase();
                model.addAttribute("pagoResumen", pagoResumen);
            }
        }
        
        // Cargar productos del carrito y calcular total (solo si no hay compra exitosa)
        List<CarritoItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> precios = new HashMap<>();
        
        if (checkoutSuccess == null || !checkoutSuccess) {
            // Solo cargar el carrito si no hay una compra exitosa (porque el carrito ya se vació)
            if (usuario != null && usuario.getId() != null) {
                try {
                    items = carritoService.listar(usuario.getId().intValue());
                    for (CarritoItemDto item : items) {
                        ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                        if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                            BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                            precios.put(item.getProductoId(), precio);
                            total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                        }
                    }
                    
                    // Si hay productos en el carrito, limpiar errores previos relacionados con carrito vacío
                    if (items != null && !items.isEmpty()) {
                        String errorActual = (String) session.getAttribute("error");
                        if (errorActual != null && errorActual.contains("carrito") && errorActual.contains("vacío")) {
                            session.removeAttribute("error");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar productos del carrito: " + e.getMessage());
                    items = new ArrayList<>();
                }
            }
        }
        
        // Validar que haya productos en el carrito (solo si no hay compra exitosa)
        if ((checkoutSuccess == null || !checkoutSuccess) && (items == null || items.isEmpty())) {
            session.setAttribute("error", "Tu carrito está vacío. Agrega productos antes de continuar.");
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(jakarta.servlet.http.HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("=== POST /checkout/finalizar recibido ===");
        
        try {
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || usuario.getId() == null) {
                System.err.println("Usuario no autenticado");
                session.setAttribute("error", "Debes iniciar sesión para completar la compra");
                return "redirect:/login";
            }
            
            System.out.println("Usuario autenticado: " + usuario.getId());
            
            // Validar que todos los pasos anteriores estén completos
            Map<String, String> direccion = (Map<String, String>) session.getAttribute("checkout_direccion");
            Map<String, String> envio = (Map<String, String>) session.getAttribute("checkout_envio");
            Map<String, Object> pago = (Map<String, Object>) session.getAttribute("checkout_pago");
            
            boolean direccionValida = direccion != null && !direccion.isEmpty();
            boolean envioValido = envio != null && !envio.isEmpty();
            boolean pagoValido = pago != null && !pago.isEmpty() && pago.containsKey("metodoPago") && pago.get("metodoPago") != null;
            
            System.out.println("Validación de pasos - Dirección: " + direccionValida + ", Envío: " + envioValido + ", Pago: " + pagoValido);
            
            if (!direccionValida) {
                System.err.println("Dirección no completada");
                session.setAttribute("error", "Por favor, completa la información de dirección antes de finalizar la compra.");
                return "redirect:/checkout/revision";
            }
            
            if (!envioValido) {
                System.err.println("Envío no completado");
                session.setAttribute("error", "Por favor, selecciona un método de envío antes de finalizar la compra.");
                return "redirect:/checkout/revision";
            }
            
            if (!pagoValido) {
                System.err.println("Pago no completado");
                session.setAttribute("error", "Por favor, selecciona un método de pago antes de finalizar la compra.");
                return "redirect:/checkout/revision";
            }
            
            // Validar que el carrito no esté vacío
            List<CarritoItemDto> items;
            try {
                items = carritoService.listar(usuario.getId().intValue());
                System.out.println("Items en carrito: " + (items != null ? items.size() : 0));
                if (items == null || items.isEmpty()) {
                    System.err.println("Carrito vacío");
                    session.setAttribute("error", "Tu carrito está vacío. Agrega productos antes de continuar.");
                    return "redirect:/checkout/revision";
                }
            } catch (Exception e) {
                System.err.println("Error al verificar el carrito: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("error", "Error al verificar el carrito: " + e.getMessage());
                return "redirect:/checkout/revision";
            }
            
            System.out.println("Procesando checkout para usuario: " + usuario.getId());
            // Procesar el checkout
            CheckoutResponseDto checkoutResponse;
            try {
                checkoutResponse = checkoutService.checkout(usuario.getId().intValue());
                System.out.println("Checkout exitoso. Venta ID: " + checkoutResponse.getVentaId() + ", Total: " + checkoutResponse.getTotal());
            } catch (Exception e) {
                System.err.println("Error al procesar checkout: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("error", "Error al procesar la compra: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
                return "redirect:/checkout/revision";
            }
            
            if (checkoutResponse == null || checkoutResponse.getVentaId() == null) {
                System.err.println("Error: La respuesta del checkout es inválida");
                session.setAttribute("error", "Error al procesar la compra. Por favor, intenta nuevamente.");
                return "redirect:/checkout/revision";
            }
            
            // Guardar información de pago ANTES de limpiar (para mostrarla en el modal)
            Map<String, Object> pagoInfo = (Map<String, Object>) session.getAttribute("checkout_pago");
            String metodoPago = null;
            if (pagoInfo != null && pagoInfo.containsKey("metodoPago")) {
                metodoPago = (String) pagoInfo.get("metodoPago");
            }
            
            // Limpiar datos de sesión del checkout
            session.removeAttribute("checkout_informacion");
            session.removeAttribute("checkout_direccion");
            session.removeAttribute("checkout_envio");
            session.removeAttribute("error"); // Limpiar errores previos
            
            // Guardar datos básicos de confirmación en la sesión para mostrar el modal
            // Los datos completos se obtendrán en checkoutRevision cuando la transacción esté confirmada
            BigDecimal totalFinal = checkoutResponse.getTotal() != null ? checkoutResponse.getTotal() : BigDecimal.ZERO;
            Integer itemsCount = checkoutResponse.getItems() != null ? checkoutResponse.getItems().size() : 0;
            
            System.out.println("Guardando datos de confirmación - Venta ID: " + checkoutResponse.getVentaId() + 
                             ", Total: $" + totalFinal + ", Items: " + itemsCount);
            
            // Guardar datos de confirmación en la sesión para mostrar el modal
            session.setAttribute("checkout_venta_id", checkoutResponse.getVentaId());
            session.setAttribute("checkout_total", totalFinal);
            session.setAttribute("checkout_fecha", java.time.LocalDate.now());
            session.setAttribute("checkout_items_count", itemsCount);
            session.setAttribute("checkout_items", new ArrayList<>()); // Se llenará en checkoutRevision
            if (metodoPago != null) {
                session.setAttribute("checkout_metodo_pago", metodoPago);
            }
            session.setAttribute("checkout_success", true);
            
            // Limpiar pago después de guardar la información necesaria
            session.removeAttribute("checkout_pago");
            
            System.out.println("Redirigiendo a /checkout/revision con modal de éxito - Datos reales guardados");
            return "redirect:/checkout/revision";
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validación en checkout: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("error", "Error de validación: " + e.getMessage());
            return "redirect:/checkout/revision";
        } catch (IllegalStateException e) {
            System.err.println("Error de estado en checkout: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("error", "Error: " + e.getMessage());
            return "redirect:/checkout/revision";
        } catch (Exception e) {
            System.err.println("Error inesperado al procesar checkout: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = "Error al procesar la compra: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            session.setAttribute("error", errorMsg);
            return "redirect:/checkout/revision";
        }
    }

    @GetMapping("/checkout/clear-success")
    public String limpiarConfirmacion(jakarta.servlet.http.HttpSession session) {
        // Limpiar todos los datos de confirmación de checkout
        session.removeAttribute("checkout_success");
        session.removeAttribute("checkout_venta_id");
        session.removeAttribute("checkout_total");
        session.removeAttribute("checkout_fecha");
        session.removeAttribute("checkout_items_count");
        session.removeAttribute("checkout_items");
        session.removeAttribute("checkout_metodo_pago");
        return "redirect:/";
    }

    // ========== CLIENTE ==========
    
    @GetMapping("/cliente/mis-compras")
    public String misCompras(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<CompraDto> compras = new ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                // Obtener todas las compras y filtrar por usuario
                List<CompraDto> todasLasCompras = comprasService.listar();
                compras = todasLasCompras.stream()
                        .filter(c -> c.getUsuarioId() != null && c.getUsuarioId().equals(usuario.getId().intValue()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Si hay error, simplemente dejar compras vacío
            compras = new ArrayList<>();
        }
        
        model.addAttribute("compras", compras);
        model.addAttribute("usuario", usuario);
        return "frontend/cliente/mis-compras";
    }

    @GetMapping("/cliente/mis-compras/{id}")
    public String detalleCompra(@PathVariable Integer id, Model model) {
        CompraDto compra = comprasService.detalle(id);
        
        if (compra != null) {
            model.addAttribute("compra", compra);
        }
        
        return "frontend/cliente/detalle-compra";
    }

    @GetMapping("/cliente/mis-compras/{id}/factura")
    public String verFactura(@PathVariable Integer id, Model model) {
        CompraDto compra = comprasService.detalle(id);
        
        if (compra == null) {
            return "redirect:/cliente/mis-compras";
        }
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("compra", compra);
        model.addAttribute("usuario", usuario);
        
        return "frontend/cliente/factura-compra";
    }

    @GetMapping("/cliente/mis-compras/{id}/factura/pdf")
    public ResponseEntity<byte[]> generarFacturaCompraPdf(@PathVariable Integer id) {
        try {
            CompraDto compra = comprasService.detalle(id);
            if (compra == null) {
                return ResponseEntity.notFound().build();
            }
            
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            byte[] pdfBytes = reporteService.generarFacturaCompra(compra, usuario);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "factura-compra-" + compra.getCompraId() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Error al generar factura: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== PEDIDOS (VENTAS) ==========
    
    @GetMapping("/cliente/pedidos")
    public String pedidos(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Verificar si hay una compra reciente para mostrar mensaje de éxito
        Boolean checkoutSuccess = (Boolean) session.getAttribute("checkout_success");
        if (checkoutSuccess != null && checkoutSuccess) {
            model.addAttribute("successMessage", "¡Tu pedido se ha registrado correctamente! Puedes verlo en la lista a continuación.");
            // Limpiar datos de confirmación de checkout
            session.removeAttribute("checkout_success");
            session.removeAttribute("checkout_venta_id");
            session.removeAttribute("checkout_total");
        }
        
        List<VentaDto> pedidos = new ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                pedidos = ventaService.porUsuario(usuario.getId().intValue());
                // Ordenar por fecha descendente (más recientes primero)
                pedidos.sort((a, b) -> {
                    if (a.getFechaVenta() == null && b.getFechaVenta() == null) return 0;
                    if (a.getFechaVenta() == null) return 1;
                    if (b.getFechaVenta() == null) return -1;
                    return b.getFechaVenta().compareTo(a.getFechaVenta());
                });
            }
        } catch (Exception e) {
            pedidos = new ArrayList<>();
            System.err.println("Error al cargar pedidos: " + e.getMessage());
            e.printStackTrace();
        }
        
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuario", usuario);
        return "frontend/cliente/pedidos";
    }
}

