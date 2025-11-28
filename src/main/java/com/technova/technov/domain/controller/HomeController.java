package com.technova.technov.domain.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.FavoritoDto;
import com.technova.technov.domain.dto.MedioDePagoDto;
import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.CaracteristicaService;
import com.technova.technov.domain.service.CarritoService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.FavoritoService;
import com.technova.technov.domain.service.MedioDePagoService;
import com.technova.technov.domain.service.ProductoService;

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
    
    @Autowired
    private SecurityUtil securityUtil;

    public HomeController(
            ProductoService productoService,
            CaracteristicaService caracteristicaService,
            CarritoService carritoService,
            FavoritoService favoritoService,
            ComprasService comprasService,
            MedioDePagoService medioDePagoService) {
        this.productoService = productoService;
        this.caracteristicaService = caracteristicaService;
        this.carritoService = carritoService;
        this.favoritoService = favoritoService;
        this.comprasService = comprasService;
        this.medioDePagoService = medioDePagoService;
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
        List<ProductoDto> productos = productoService.porCategoria(categoria);
        model.addAttribute("categoria", categoria);
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/categoria/categoria";
    }

    @GetMapping("/categoria/celulares")
    public String celulares(Model model) {
        List<ProductoDto> productos = productoService.porCategoria("Celulares");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/categoria/celulares";
    }

    @GetMapping("/categoria/portatiles")
    public String portatiles(Model model) {
        List<ProductoDto> productos = productoService.porCategoria("Portátiles");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/categoria/portatiles";
    }

    // ========== MARCAS ==========
    
    @GetMapping("/marca/{marca}")
    public String marca(@PathVariable String marca, Model model) {
        String marcaCapitalizada = marca.substring(0, 1).toUpperCase() + marca.substring(1).toLowerCase();
        List<ProductoDto> productos = productoService.porMarca(marcaCapitalizada);
        model.addAttribute("productos", enriquecerProductos(productos));
        
        switch (marca.toLowerCase()) {
            case "apple": return "frontend/marca/apple";
            case "samsung": return "frontend/marca/samsung";
            case "motorola": return "frontend/marca/motorola";
            case "xiaomi": return "frontend/marca/xiaomi";
            case "oppo": return "frontend/marca/oppo";
            case "lenovo": return "frontend/marca/lenovo";
            default: return "frontend/marca/apple";
        }
    }

    @GetMapping("/marca/apple")
    public String marcaApple(Model model) {
        List<ProductoDto> productos = productoService.porMarca("Apple");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/marca/apple";
    }

    @GetMapping("/marca/samsung")
    public String marcaSamsung(Model model) {
        List<ProductoDto> productos = productoService.porMarca("Samsung");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/marca/samsung";
    }

    @GetMapping("/marca/motorola")
    public String marcaMotorola(Model model) {
        List<ProductoDto> productos = productoService.porMarca("Motorola");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/marca/motorola";
    }

    @GetMapping("/marca/xiaomi")
    public String marcaXiaomi(Model model) {
        List<ProductoDto> productos = productoService.porMarca("Xiaomi");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/marca/xiaomi";
    }

    @GetMapping("/marca/oppo")
    public String marcaOppo(Model model) {
        List<ProductoDto> productos = productoService.porMarca("OPPO");
        model.addAttribute("productos", enriquecerProductos(productos));
        return "frontend/marca/oppo";
    }

    @GetMapping("/marca/lenovo")
    public String marcaLenovo(Model model) {
        List<ProductoDto> productos = productoService.porMarca("Lenovo");
        model.addAttribute("productos", enriquecerProductos(productos));
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
        double total = 0.0;
        
        try {
            if (usuario.getId() != null) {
                items = carritoService.listar(usuario.getId().intValue());
                // Calcular total del carrito
                for (CarritoItemDto item : items) {
                    // Obtener precio del producto
                    ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                    if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                        BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                        total += precio.doubleValue() * item.getCantidad();
                    }
                }
            }
        } catch (Exception e) {
            // Si hay error, simplemente dejar items vacío
            items = new ArrayList<>();
        }
        
        model.addAttribute("productos", items);
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
    public String checkoutInformacion(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "informacion");
        model.addAttribute("usuario", usuario);
        
        List<CarritoItemDto> items = new ArrayList<>();
        double total = 0.0;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    total += precio.doubleValue() * item.getCantidad();
                }
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @GetMapping("/checkout/direccion")
    public String checkoutDireccion(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "direccion");
        
        List<CarritoItemDto> items = new ArrayList<>();
        double total = 0.0;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    total += precio.doubleValue() * item.getCantidad();
                }
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @GetMapping("/checkout/envio")
    public String checkoutEnvio(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "envio");
        
        List<CarritoItemDto> items = new ArrayList<>();
        double total = 0.0;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    total += precio.doubleValue() * item.getCantidad();
                }
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @GetMapping("/checkout/pago")
    public String checkoutPago(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "pago");
        
        List<CarritoItemDto> items = new ArrayList<>();
        double total = 0.0;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    total += precio.doubleValue() * item.getCantidad();
                }
            }
        }
        
        // Obtener métodos de pago disponibles
        List<MedioDePagoDto> metodosDisponibles = medioDePagoService.listar();
        List<Map<String, String>> metodosFormato = metodosDisponibles.stream()
                .map(metodo -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("value", metodo.getMetodoPago().toLowerCase().replace(" ", "_"));
                    m.put("label", metodo.getMetodoPago());
                    return m;
                })
                .collect(Collectors.toList());
        
        model.addAttribute("productos", items);
        model.addAttribute("total", total);
        model.addAttribute("metodosDisponibles", metodosFormato);
        return "frontend/checkout/layout";
    }

    @GetMapping("/checkout/revision")
    public String checkoutRevision(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "revision");
        model.addAttribute("usuario", usuario);
        
        List<CarritoItemDto> items = new ArrayList<>();
        double total = 0.0;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    total += precio.doubleValue() * item.getCantidad();
                }
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
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
}

