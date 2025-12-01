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
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.technova.technov.domain.dto.MedioDePagoDto;
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
        List<ProductoDto> productos = productoService.porCategoria(categoria);
        model.addAttribute("categoria", categoria);
        model.addAttribute("productos", enriquecerProductos(productos));
        
        if (usuario != null && "cliente".equalsIgnoreCase(usuario.getRole())) {
            model.addAttribute("usuario", usuario);
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
        
        String templateBase = "frontend/marca/" + marca.toLowerCase();
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
            return templateBase + "-autenticado";
        }
        return templateBase;
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
    public String checkoutInformacion(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "informacion");
        model.addAttribute("usuario", usuario);
        
        List<CarritoItemDto> items = new ArrayList<>();
        Map<Integer, BigDecimal> precios = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    precios.put(item.getProductoId(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                }
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
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    precios.put(item.getProductoId(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                }
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
        // Guardar datos en sesión
        Map<String, String> direccionData = new HashMap<>();
        if (departamento != null) direccionData.put("departamento", departamento);
        if (ciudad != null) direccionData.put("ciudad", ciudad);
        if (direccion != null) direccionData.put("direccion", direccion);
        if (localidad != null) direccionData.put("localidad", localidad);
        if (barrio != null) direccionData.put("barrio", barrio);
        session.setAttribute("checkout_direccion", direccionData);
        
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
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    precios.put(item.getProductoId(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                }
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
        // Guardar datos en sesión
        Map<String, String> envio = new HashMap<>();
        if (transportadora != null) envio.put("transportadora", transportadora);
        if (fechaEnvio != null) envio.put("fechaEnvio", fechaEnvio);
        session.setAttribute("checkout_envio", envio);
        
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
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    precios.put(item.getProductoId(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                }
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
        // Guardar datos en sesión
        Map<String, Object> pago = new HashMap<>();
        if (metodoPago != null) pago.put("metodoPago", metodoPago);
        pago.put("datosPago", datosPago);
        session.setAttribute("checkout_pago", pago);
        
        return "redirect:/checkout/revision";
    }

    @GetMapping("/checkout/revision")
    public String checkoutRevision(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        model.addAttribute("step", "revision");
        model.addAttribute("usuario", usuario);
        
        // Cargar datos de sesión
        Map<String, String> informacion = (Map<String, String>) session.getAttribute("checkout_informacion");
        Map<String, String> direccion = (Map<String, String>) session.getAttribute("checkout_direccion");
        Map<String, String> envio = (Map<String, String>) session.getAttribute("checkout_envio");
        Map<String, Object> pago = (Map<String, Object>) session.getAttribute("checkout_pago");
        
        if (informacion != null) {
            model.addAttribute("informacion", informacion);
        }
        if (direccion != null) {
            model.addAttribute("direccion", direccion);
        }
        if (envio != null) {
            model.addAttribute("envio", envio);
        }
        if (pago != null) {
            model.addAttribute("pago", pago);
            String metodoPago = (String) pago.get("metodoPago");
            if (metodoPago != null) {
                String pagoResumen = metodoPago.replace("_", " ").toUpperCase();
                model.addAttribute("pagoResumen", pagoResumen);
            }
        }
        
        List<CarritoItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> precios = new HashMap<>();
        
        if (usuario != null && usuario.getId() != null) {
            items = carritoService.listar(usuario.getId().intValue());
            for (CarritoItemDto item : items) {
                ProductoDto producto = productoService.productoPorId(item.getProductoId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precio = producto.getCaracteristica().getPrecioVenta();
                    precios.put(item.getProductoId(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 1)));
                }
            }
        }
        
        model.addAttribute("productos", items);
        model.addAttribute("precios", precios);
        model.addAttribute("total", total);
        return "frontend/checkout/layout";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(jakarta.servlet.http.HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || usuario.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para completar la compra");
            return "redirect:/login";
        }
        
        try {
            // Procesar el checkout
            CheckoutResponseDto checkoutResponse = checkoutService.checkout(usuario.getId().intValue());
            
            // Limpiar datos de sesión
            session.removeAttribute("checkout_informacion");
            session.removeAttribute("checkout_direccion");
            session.removeAttribute("checkout_envio");
            session.removeAttribute("checkout_pago");
            
            // Guardar el ID de la venta en la sesión para mostrarlo en la confirmación
            session.setAttribute("checkout_venta_id", checkoutResponse.getVentaId());
            session.setAttribute("checkout_total", checkoutResponse.getTotal());
            
            return "redirect:/checkout/confirmacion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/checkout/revision";
        }
    }

    @GetMapping("/checkout/confirmacion")
    public String confirmacionCompra(Model model, jakarta.servlet.http.HttpSession session) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || usuario.getId() == null) {
            return "redirect:/login";
        }
        
        // Obtener datos de la sesión
        Integer ventaId = (Integer) session.getAttribute("checkout_venta_id");
        BigDecimal total = (BigDecimal) session.getAttribute("checkout_total");
        
        // Limpiar la sesión después de obtener los datos
        session.removeAttribute("checkout_venta_id");
        session.removeAttribute("checkout_total");
        
        // Si no hay datos de compra, redirigir a mis compras
        if (ventaId == null) {
            return "redirect:/cliente/mis-compras";
        }
        
        // Crear un objeto simple para mostrar en la confirmación
        model.addAttribute("ventaId", ventaId);
        model.addAttribute("total", total != null ? total : BigDecimal.ZERO);
        model.addAttribute("usuario", usuario);
        
        return "frontend/checkout/confirmacion";
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
    public String pedidos(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<VentaDto> pedidos = new ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                pedidos = ventaService.porUsuario(usuario.getId().intValue());
                // Log para debug
                System.out.println("Pedidos encontrados para usuario " + usuario.getId() + ": " + pedidos.size());
                for (VentaDto p : pedidos) {
                    System.out.println("Pedido #" + p.getVentaId() + " - Total: " + p.getTotal() + " - Items: " + (p.getItems() != null ? p.getItems().size() : 0));
                    if (p.getItems() != null) {
                        for (var item : p.getItems()) {
                            System.out.println("  - " + item.getNombreProducto() + " x" + item.getCantidad() + " = $" + item.getPrecioLinea());
                        }
                    }
                }
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

