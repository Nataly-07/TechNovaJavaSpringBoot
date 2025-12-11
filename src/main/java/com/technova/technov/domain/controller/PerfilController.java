package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.AtencionClienteService;
import com.technova.technov.domain.service.CarritoService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.FavoritoService;
import com.technova.technov.domain.service.NotificacionService;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.UsuarioService;
import com.technova.technov.domain.service.VentaService;
import com.technova.technov.domain.service.ProveedorService;
import com.technova.technov.domain.service.PagoService;
import com.technova.technov.domain.service.MensajeDirectoService;
import com.technova.technov.domain.repository.CaracteristicaRepository;
import com.technova.technov.domain.repository.AtencionClienteRepository;
import org.modelmapper.ModelMapper;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para los perfiles de usuario según su rol.
 */
@Controller
public class PerfilController {

    private final FavoritoService favoritoService;
    private final CarritoService carritoService;
    private final ComprasService comprasService;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final AtencionClienteService atencionClienteService;
    private final NotificacionService notificacionService;
    private final VentaService ventaService;
    private final ProveedorService proveedorService;
    private final PagoService pagoService;
    private final MensajeDirectoService mensajeDirectoService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;
    
    @Autowired
    private AtencionClienteRepository atencionClienteRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    public PerfilController(
            FavoritoService favoritoService,
            CarritoService carritoService,
            ComprasService comprasService,
            UsuarioService usuarioService,
            ProductoService productoService,
            AtencionClienteService atencionClienteService,
            NotificacionService notificacionService,
            VentaService ventaService,
            ProveedorService proveedorService,
            PagoService pagoService,
            MensajeDirectoService mensajeDirectoService) {
        this.favoritoService = favoritoService;
        this.carritoService = carritoService;
        this.comprasService = comprasService;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.atencionClienteService = atencionClienteService;
        this.notificacionService = notificacionService;
        this.ventaService = ventaService;
        this.proveedorService = proveedorService;
        this.pagoService = pagoService;
        this.mensajeDirectoService = mensajeDirectoService;
    }

    @GetMapping("/cliente/perfil")
    public String perfilCliente(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener estadísticas del cliente
        Long usuarioId = usuario.getId();
        int favoritosCount = favoritoService.listarPorUsuario(usuarioId).size();
        int carritoCount = carritoService.listar(usuarioId.intValue()).size();
        
        List<com.technova.technov.domain.dto.CompraDto> todasLasCompras = comprasService.listar();
        int comprasCount = (int) todasLasCompras.stream()
                .filter(c -> c.getUsuarioId() != null && c.getUsuarioId().equals(usuarioId.intValue()))
                .count();
        
        int pedidosCount = 0;
        try {
            List<com.technova.technov.domain.dto.VentaDto> pedidos = ventaService.porUsuario(usuarioId.intValue());
            pedidosCount = pedidos != null ? pedidos.size() : 0;
        } catch (Exception e) {
            pedidosCount = 0;
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("favoritosCount", favoritosCount);
        model.addAttribute("carritoCount", carritoCount);
        model.addAttribute("comprasCount", comprasCount);
        model.addAttribute("pedidosCount", pedidosCount);
        model.addAttribute("notificacionesCount", 0);
        model.addAttribute("mediosPagoCount", 0);
        
        return "frontend/cliente/perfil";
    }

    @GetMapping("/empleado/perfil")
    public String perfilEmpleado(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener estadísticas del empleado
        List<UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
        int clientesCount = (int) todosUsuarios.stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRole()) || "cliente".equalsIgnoreCase(u.getRole()))
                .count();
        int productosCount = productoService.listarProductos().size();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("clientesCount", clientesCount);
        model.addAttribute("productosCount", productosCount);
        return "frontend/empleado/perfil";
    }

    @GetMapping("/admin/perfil")
    public String perfilAdmin(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener estadísticas del admin
        int usersCount = usuarioService.listarUsuarios().size();
        int productosCount = productoService.listarProductos().size();
        int reportesDisponibles = 3; // Reportes: productos, usuarios, ventas
        int proveedoresCount = proveedorService.listarProveedores().size();
        int pedidosProcesados = ventaService.listar().size();
        int transaccionesProcesadas = pagoService.listarTodos().size();
        
        // Calcular mensajes pendientes (tickets abiertos + en proceso)
        int mensajesPendientes = 0;
        try {
            int ticketsAbiertos = atencionClienteService.listarPorEstado("abierto").size();
            int ticketsEnProceso = atencionClienteService.listarPorEstado("en_proceso").size();
            mensajesPendientes = ticketsAbiertos + ticketsEnProceso;
        } catch (Exception e) {
            mensajesPendientes = 0;
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("usersCount", usersCount);
        model.addAttribute("productosCount", productosCount);
        model.addAttribute("reportesDisponibles", reportesDisponibles);
        model.addAttribute("proveedoresCount", proveedoresCount);
        model.addAttribute("mensajesPendientes", mensajesPendientes);
        model.addAttribute("pedidosProcesados", pedidosProcesados);
        model.addAttribute("transaccionesProcesadas", transaccionesProcesadas);
        
        return "frontend/admin/perfil";
    }

    @GetMapping("/cliente/atencion-cliente")
    public String atencionCliente(Model model) {
        // Inicializar todas las variables con valores por defecto ANTES de cualquier operación
        UsuarioDto usuario = null;
        List<com.technova.technov.domain.dto.AtencionClienteDto> tickets = new java.util.ArrayList<>();
        List<com.technova.technov.domain.dto.MensajeDirectoDto> conversaciones = new java.util.ArrayList<>();
        
        // Agregar atributos al modelo INMEDIATAMENTE para asegurar que siempre estén presentes
        model.addAttribute("usuario", usuario);
        model.addAttribute("tickets", tickets);
        model.addAttribute("conversaciones", conversaciones);
        
        try {
            // Obtener usuario autenticado
            usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
                return "redirect:/login";
            }
            
            // Actualizar usuario en el modelo
            model.addAttribute("usuario", usuario);
            
            // Cargar tickets del usuario - con timeout implícito
            if (usuario.getId() != null) {
                try {
                    List<com.technova.technov.domain.dto.AtencionClienteDto> ticketsTemp = atencionClienteService.listarPorUsuario(usuario.getId().intValue());
                    if (ticketsTemp != null) {
                        tickets = ticketsTemp;
                    }
                    model.addAttribute("tickets", tickets);
                } catch (Exception e) {
                    System.err.println("Error al cargar tickets: " + e.getMessage());
                    // Mantener lista vacía en el modelo
                    model.addAttribute("tickets", new java.util.ArrayList<>());
                }
            }
            
            // Cargar conversaciones del usuario - simplificado para evitar problemas
            if (usuario.getId() != null) {
                try {
                    List<com.technova.technov.domain.dto.MensajeDirectoDto> todosMensajes = mensajeDirectoService.listarPorUsuario(usuario.getId());
                    if (todosMensajes != null && !todosMensajes.isEmpty()) {
                        // Procesamiento simplificado y seguro
                        java.util.Map<String, com.technova.technov.domain.dto.MensajeDirectoDto> ultimosMensajes = new java.util.HashMap<>();
                        
                        for (com.technova.technov.domain.dto.MensajeDirectoDto mensaje : todosMensajes) {
                            try {
                                if (mensaje != null && mensaje.getConversationId() != null && !mensaje.getConversationId().isEmpty()) {
                                    com.technova.technov.domain.dto.MensajeDirectoDto existente = ultimosMensajes.get(mensaje.getConversationId());
                                    if (existente == null) {
                                        ultimosMensajes.put(mensaje.getConversationId(), mensaje);
                                    } else if (mensaje.getCreatedAt() != null && existente.getCreatedAt() != null) {
                                        try {
                                            if (mensaje.getCreatedAt().isAfter(existente.getCreatedAt())) {
                                                ultimosMensajes.put(mensaje.getConversationId(), mensaje);
                                            }
                                        } catch (Exception e) {
                                            // Si hay error comparando fechas, mantener el existente
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Continuar con el siguiente mensaje si hay error
                                continue;
                            }
                        }
                        
                        conversaciones = new java.util.ArrayList<>(ultimosMensajes.values());
                        
                        // Ordenar de forma segura
                        try {
                            conversaciones.sort((a, b) -> {
                                try {
                                    if (a == null || b == null) return 0;
                                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                                    if (a.getCreatedAt() == null) return 1;
                                    if (b.getCreatedAt() == null) return -1;
                                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                                } catch (Exception e) {
                                    return 0;
                                }
                            });
                        } catch (Exception e) {
                            // Si hay error ordenando, mantener el orden original
                        }
                    }
                    model.addAttribute("conversaciones", conversaciones);
                } catch (Exception e) {
                    System.err.println("Error al cargar conversaciones: " + e.getMessage());
                    // Mantener lista vacía en el modelo
                    model.addAttribute("conversaciones", new java.util.ArrayList<>());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error crítico en atencionCliente: " + e.getMessage());
            // Asegurar que el modelo siempre tenga valores válidos
            if (!model.containsAttribute("usuario")) {
                model.addAttribute("usuario", null);
            }
            if (!model.containsAttribute("tickets")) {
                model.addAttribute("tickets", new java.util.ArrayList<>());
            }
            if (!model.containsAttribute("conversaciones")) {
                model.addAttribute("conversaciones", new java.util.ArrayList<>());
            }
        }
        
        // SIEMPRE retornar el template, incluso si hay errores
        return "frontend/cliente/atencion-cliente";
    }

    @GetMapping("/cliente/notificaciones")
    public String notificaciones(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<com.technova.technov.domain.dto.NotificacionDto> notificaciones = new java.util.ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                notificaciones = notificacionService.listarPorUsuario(usuario.getId());
            }
        } catch (Exception e) {
            notificaciones = new java.util.ArrayList<>();
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("notificaciones", notificaciones);
        return "frontend/cliente/notificaciones";
    }

    @GetMapping("/empleado/usuarios")
    public String usuariosEmpleado(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener todos los usuarios y filtrar solo los clientes
        List<UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
        List<UsuarioDto> clientes = todosUsuarios.stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRole()) || "cliente".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarios", clientes);
        model.addAttribute("totalUsuarios", clientes.size());
        
        return "frontend/empleado/usuarios";
    }

    @GetMapping("/empleado/productos")
    public String productosEmpleado(Model model) {
        try {
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
                return "redirect:/login";
            }
            
            // Obtener todos los productos
            List<com.technova.technov.domain.dto.ProductoDto> productos = productoService.listarProductos();
            
            // Validar y limpiar productos antes de pasarlos al template
            if (productos != null) {
                productos = productos.stream()
                    .filter(p -> p != null && p.getId() != null)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                productos = new java.util.ArrayList<>();
            }
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("productos", productos);
            model.addAttribute("totalProductos", productos.size());
            
            return "frontend/empleado/productos";
        } catch (Exception e) {
            System.err.println("Error en productosEmpleado: " + e.getMessage());
            e.printStackTrace();
            // Retornar una lista vacía en caso de error
            model.addAttribute("productos", new java.util.ArrayList<>());
            model.addAttribute("totalProductos", 0);
            UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            model.addAttribute("usuario", usuario);
            return "frontend/empleado/productos";
        }
    }

    @GetMapping("/empleado/pedidos")
    public String pedidosEmpleado(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer usuarioId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String fechaDesde,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String fechaHasta,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String busqueda,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String categoria,
            Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener usuarios y crear mapas ANTES de aplicar filtros (necesarios para búsqueda general)
        List<UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
        java.util.Map<Integer, String> nombresUsuarios = new java.util.HashMap<>();
        for (UsuarioDto u : todosUsuarios) {
            if (u.getId() != null) {
                nombresUsuarios.put(u.getId().intValue(), u.getName() != null ? u.getName() : "Usuario sin nombre");
            }
        }
        
        // Obtener TODOS los pedidos del sistema
        List<com.technova.technov.domain.dto.VentaDto> pedidos = new java.util.ArrayList<>();
        try {
            // Usar listar() para obtener todos los pedidos activos del sistema
            pedidos = ventaService.listar();
            
            // Validar que no haya pedidos nulos
            if (pedidos != null) {
                pedidos = pedidos.stream()
                    .filter(p -> p != null && p.getVentaId() != null)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                pedidos = new java.util.ArrayList<>();
            }
            
            // Aplicar filtros
            if (usuarioId != null) {
                pedidos = ventaService.porUsuario(usuarioId);
            }

            if (fechaDesde != null && !fechaDesde.isEmpty() && fechaHasta != null && !fechaHasta.isEmpty()) {
                try {
                    java.time.LocalDate desde = java.time.LocalDate.parse(fechaDesde);
                    java.time.LocalDate hasta = java.time.LocalDate.parse(fechaHasta);
                    pedidos = pedidos.stream()
                            .filter(p -> p.getFechaVenta() != null && 
                                       !p.getFechaVenta().isBefore(desde) && 
                                       !p.getFechaVenta().isAfter(hasta))
                            .collect(java.util.stream.Collectors.toList());
                } catch (Exception e) {
                    // Si hay error en el parseo de fechas, ignorar el filtro
                }
            }

            if (busqueda != null && !busqueda.isEmpty()) {
                final String busquedaLower = busqueda.toLowerCase();
                pedidos = pedidos.stream()
                        .filter(p -> {
                            // Buscar en ID de pedido
                            if (p.getVentaId() != null && String.valueOf(p.getVentaId()).contains(busqueda)) {
                                return true;
                            }
                            
                            // Buscar en nombre de usuario
                            if (p.getUsuarioId() != null && nombresUsuarios.containsKey(p.getUsuarioId())) {
                                String nombreUsuario = nombresUsuarios.get(p.getUsuarioId());
                                if (nombreUsuario != null && nombreUsuario.toLowerCase().contains(busquedaLower)) {
                                    return true;
                                }
                            }
                            
                            // Buscar en correo de usuario (necesitamos obtener el correo)
                            if (p.getUsuarioId() != null) {
                                for (UsuarioDto u : todosUsuarios) {
                                    if (u.getId() != null && u.getId().intValue() == p.getUsuarioId()) {
                                        if (u.getEmail() != null && u.getEmail().toLowerCase().contains(busquedaLower)) {
                                            return true;
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            // Buscar en items del pedido (nombre de producto)
                            if (p.getItems() != null) {
                                boolean encontradoEnItems = p.getItems().stream()
                                        .anyMatch(item -> {
                                            if (item.getNombreProducto() != null && 
                                                item.getNombreProducto().toLowerCase().contains(busquedaLower)) {
                                                return true;
                                            }
                                            return false;
                                        });
                                if (encontradoEnItems) {
                                    return true;
                                }
                            }
                            
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            // Crear mapa de productoId -> categoria para filtrar por categoría
            java.util.Map<Integer, String> productoIdToCategoria = new java.util.HashMap<>();
            if (categoria != null && !categoria.isEmpty()) {
                List<com.technova.technov.domain.dto.ProductoDto> todosProductos = productoService.listarProductos();
                for (com.technova.technov.domain.dto.ProductoDto prod : todosProductos) {
                    if (prod.getId() != null && prod.getCaracteristica() != null && prod.getCaracteristica().getCategoria() != null) {
                        productoIdToCategoria.put(prod.getId(), prod.getCaracteristica().getCategoria());
                    }
                }
                
                // Filtrar pedidos que contengan productos de la categoría seleccionada
                final String categoriaLower = categoria != null ? categoria.toLowerCase() : "";
                pedidos = pedidos.stream()
                        .filter(p -> {
                            if (p.getItems() != null) {
                                return p.getItems().stream()
                                        .anyMatch(item -> {
                                            if (item.getProductoId() != null) {
                                                String cat = productoIdToCategoria.get(item.getProductoId());
                                                return cat != null && cat.equalsIgnoreCase(categoriaLower);
                                            }
                                            return false;
                                        });
                            }
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar pedidos en PerfilController: " + e.getMessage());
            e.printStackTrace();
            pedidos = new java.util.ArrayList<>();
            model.addAttribute("errorCargaPedidos", "Hubo un error al cargar los pedidos.");
        }
        
        // Calcular estadísticas con TODOS los pedidos (sin filtros)
        List<com.technova.technov.domain.dto.VentaDto> todosLosPedidos = ventaService.listar();
        long totalPedidos = todosLosPedidos.size();
        java.math.BigDecimal totalVentas = todosLosPedidos.stream()
                .map(p -> p.getTotal() != null ? p.getTotal() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        // Pedidos del mes actual
        java.time.LocalDate inicioMes = java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate finMes = java.time.LocalDate.now();
        long pedidosEsteMes = todosLosPedidos.stream()
                .filter(p -> p.getFechaVenta() != null && 
                           !p.getFechaVenta().isBefore(inicioMes) && 
                           !p.getFechaVenta().isAfter(finMes))
                .count();
        
        // Obtener solo los usuarios que son CLIENTES para el filtro
        List<UsuarioDto> usuariosClientes = todosUsuarios.stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRole()) || "cliente".equalsIgnoreCase(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        
        // Obtener todas las categorías disponibles para el filtro
        List<String> categorias = caracteristicaRepository.listarCategorias();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoria", categoria);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("pedidosEsteMes", pedidosEsteMes);
        model.addAttribute("usuarios", usuariosClientes); // Solo clientes en el filtro
        model.addAttribute("nombresUsuarios", nombresUsuarios); // Todos los usuarios para mostrar nombres
        model.addAttribute("categorias", categorias); // Lista de categorías para el filtro
        
        return "frontend/empleado/pedidos";
    }

    @GetMapping("/empleado/atencion-cliente")
    public String atencionClienteEmpleado(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String busqueda,
            Model model) {
        // Inicializar todas las variables con valores por defecto ANTES de cualquier operación
        UsuarioDto usuario = null;
        List<com.technova.technov.domain.dto.AtencionClienteDto> tickets = new java.util.ArrayList<>();
        List<com.technova.technov.domain.dto.MensajeDirectoDto> conversaciones = new java.util.ArrayList<>();
        long totalConsultas = 0;
        long pendientes = 0;
        long mensajes = 0;
        long noLeidos = 0;
        java.util.Map<Integer, String> nombresUsuarios = new java.util.HashMap<>();
        String estadoModelo = estado != null ? estado : "todas";
        
        // Agregar atributos al modelo INMEDIATAMENTE para asegurar que siempre estén presentes
        model.addAttribute("usuario", usuario);
        model.addAttribute("tickets", tickets);
        model.addAttribute("conversaciones", conversaciones);
        model.addAttribute("estado", estadoModelo);
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("totalConsultas", totalConsultas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("noLeidos", noLeidos);
        model.addAttribute("nombresUsuarios", nombresUsuarios);
        
        try {
            usuario = securityUtil.getUsuarioAutenticado().orElse(null);
            
            if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
                return "redirect:/login";
            }
            
            // Actualizar usuario en el modelo
            model.addAttribute("usuario", usuario);
            
            // Obtener todos los tickets o filtrar por estado
            try {
                // Si el estado es "todas" o null, cargar TODOS los tickets
                if (estado == null || estado.isEmpty() || "todas".equalsIgnoreCase(estado)) {
                    // Usar el servicio para obtener todos los tickets (asegura mapeo correcto y transacción)
                    tickets = atencionClienteService.listarTodos();
                } else if ("pendientes".equalsIgnoreCase(estado)) {
                    // Si el estado es "pendientes", filtrar por "abierto" y "en_proceso"
                    List<com.technova.technov.domain.dto.AtencionClienteDto> abiertos = atencionClienteService.listarPorEstado("abierto");
                    List<com.technova.technov.domain.dto.AtencionClienteDto> enProceso = atencionClienteService.listarPorEstado("en_proceso");
                    tickets = new java.util.ArrayList<>(abiertos);
                    tickets.addAll(enProceso);
                    // Ordenar por fecha descendente después de combinar
                    tickets.sort((a, b) -> {
                        if (a.getFechaConsulta() == null && b.getFechaConsulta() == null) return 0;
                        if (a.getFechaConsulta() == null) return 1;
                        if (b.getFechaConsulta() == null) return -1;
                        return b.getFechaConsulta().compareTo(a.getFechaConsulta());
                    });
                } else {
                    // Filtrar por estado específico
                    tickets = atencionClienteService.listarPorEstado(estado);
                }
                
                // Los tickets ya vienen ordenados del servicio, pero asegurémonos de que estén ordenados
                if (tickets != null && !tickets.isEmpty()) {
                    tickets.sort((a, b) -> {
                        if (a.getFechaConsulta() == null && b.getFechaConsulta() == null) return 0;
                        if (a.getFechaConsulta() == null) return 1;
                        if (b.getFechaConsulta() == null) return -1;
                        return b.getFechaConsulta().compareTo(a.getFechaConsulta());
                    });
                }
                
                // Aplicar filtro de búsqueda por nombre de cliente SOLO si hay búsqueda activa
                if (busqueda != null && !busqueda.isEmpty() && !busqueda.trim().isEmpty()) {
                    final String busquedaLower = busqueda.toLowerCase().trim();
                    List<UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
                    java.util.Map<Integer, String> nombresUsuariosTemp = new java.util.HashMap<>();
                    for (UsuarioDto u : todosUsuarios) {
                        if (u.getId() != null) {
                            nombresUsuariosTemp.put(u.getId().intValue(), u.getName() != null ? u.getName() : "");
                        }
                    }
                    
                    if (tickets != null) {
                        tickets = tickets.stream()
                                .filter(t -> {
                                // Buscar en tema y descripción
                                if ((t.getTema() != null && t.getTema().toLowerCase().contains(busquedaLower)) ||
                                    (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(busquedaLower))) {
                                    return true;
                                }
                                // Buscar en nombre de usuario
                                if (t.getUsuarioId() != null && nombresUsuariosTemp.containsKey(t.getUsuarioId())) {
                                    String nombreUsuario = nombresUsuariosTemp.get(t.getUsuarioId());
                                    if (nombreUsuario != null && nombreUsuario.toLowerCase().contains(busquedaLower)) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .collect(java.util.stream.Collectors.toList());
                    }
                }
                
                // Asegurar que tickets nunca sea null
                if (tickets == null) {
                    tickets = new java.util.ArrayList<>();
                }
                
                // Actualizar tickets en el modelo
                model.addAttribute("tickets", tickets);
                
            } catch (Exception e) {
                System.err.println("Error al cargar tickets en PerfilController: " + e.getMessage());
                e.printStackTrace();
                tickets = new java.util.ArrayList<>();
                model.addAttribute("tickets", tickets);
            }
        
        // Calcular estadísticas de CONSULTAS (tickets de atención al cliente)
        try {
            // Usar el mismo método que se usa para cargar los tickets para asegurar consistencia
            List<com.technova.technov.domain.dto.AtencionClienteDto> todosTickets = atencionClienteRepository.findAllByOrderByFechaConsultaDesc().stream()
                    .map(t -> {
                        com.technova.technov.domain.dto.AtencionClienteDto dto = modelMapper.map(t, com.technova.technov.domain.dto.AtencionClienteDto.class);
                        if (t.getUsuario() != null) {
                            dto.setUsuarioId(t.getUsuario().getId().intValue());
                        }
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            totalConsultas = todosTickets.size();
            // Pendientes: tickets con estado "abierto" o "en_proceso"
            pendientes = todosTickets.stream()
                    .filter(t -> "abierto".equalsIgnoreCase(t.getEstado()) || "en_proceso".equalsIgnoreCase(t.getEstado()))
                    .count();
            
            model.addAttribute("totalConsultas", totalConsultas);
            model.addAttribute("pendientes", pendientes);
        } catch (Exception e) {
            System.err.println("Error al calcular estadísticas de consultas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("totalConsultas", 0);
            model.addAttribute("pendientes", 0);
        }
        
        // Calcular estadísticas de MENSAJES DIRECTOS
        try {
            List<com.technova.technov.domain.dto.MensajeDirectoDto> todosMensajes = mensajeDirectoService.listarTodos();
            
            // Obtener conversaciones únicas con el último mensaje de cada una
            java.util.Map<String, com.technova.technov.domain.dto.MensajeDirectoDto> ultimosMensajes = new java.util.HashMap<>();
            
            for (com.technova.technov.domain.dto.MensajeDirectoDto mensaje : todosMensajes) {
                if (mensaje.getConversationId() != null) {
                    com.technova.technov.domain.dto.MensajeDirectoDto existente = ultimosMensajes.get(mensaje.getConversationId());
                    if (existente == null || 
                        (mensaje.getCreatedAt() != null && existente.getCreatedAt() != null &&
                         mensaje.getCreatedAt().isAfter(existente.getCreatedAt()))) {
                        ultimosMensajes.put(mensaje.getConversationId(), mensaje);
                    }
                }
            }
            conversaciones = new java.util.ArrayList<>(ultimosMensajes.values());
            // Contar conversaciones únicas, no mensajes individuales
            mensajes = conversaciones.size();
            // Contar conversaciones donde el ÚLTIMO mensaje no está leído
            // Esto coincide con lo que se muestra en la lista del frontend
            noLeidos = conversaciones.stream()
                    .filter(conversacion -> {
                        if (conversacion == null) return false;
                        // Un mensaje se considera leído si:
                        // - isRead = true
                        // - estado = "respondido"
                        // - senderType = "empleado" (enviado por empleado)
                        boolean esLeido = conversacion.isRead() || 
                                         "respondido".equalsIgnoreCase(conversacion.getEstado()) ||
                                         "empleado".equalsIgnoreCase(conversacion.getSenderType());
                        return !esLeido;
                    })
                    .count();
            // Ordenar por fecha descendente
            conversaciones.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            
            model.addAttribute("conversaciones", conversaciones);
            model.addAttribute("mensajes", mensajes);
            model.addAttribute("noLeidos", noLeidos);
        } catch (Exception e) {
            System.err.println("Error al cargar mensajes directos: " + e.getMessage());
            e.printStackTrace();
            conversaciones = new java.util.ArrayList<>();
            model.addAttribute("conversaciones", conversaciones);
            model.addAttribute("mensajes", 0);
            model.addAttribute("noLeidos", 0);
        }
        
        // Obtener nombres de usuarios para mostrar en la vista
        try {
            List<UsuarioDto> todosUsuarios = usuarioService.listarUsuarios();
            nombresUsuarios = new java.util.HashMap<>();
            for (UsuarioDto u : todosUsuarios) {
                if (u.getId() != null) {
                    nombresUsuarios.put(u.getId().intValue(), u.getName() != null ? u.getName() : "Usuario sin nombre");
                }
            }
            model.addAttribute("nombresUsuarios", nombresUsuarios);
        } catch (Exception e) {
            System.err.println("Error al cargar nombres de usuarios: " + e.getMessage());
            e.printStackTrace();
            nombresUsuarios = new java.util.HashMap<>();
            model.addAttribute("nombresUsuarios", nombresUsuarios);
        }
        
        } catch (Exception e) {
            System.err.println("Error crítico en atencionClienteEmpleado: " + e.getMessage());
            e.printStackTrace();
            // Asegurar que el modelo siempre tenga valores válidos
            if (!model.containsAttribute("usuario")) {
                model.addAttribute("usuario", null);
            }
            if (!model.containsAttribute("tickets")) {
                model.addAttribute("tickets", new java.util.ArrayList<>());
            }
            if (!model.containsAttribute("conversaciones")) {
                model.addAttribute("conversaciones", new java.util.ArrayList<>());
            }
            if (!model.containsAttribute("totalConsultas")) {
                model.addAttribute("totalConsultas", 0);
            }
            if (!model.containsAttribute("pendientes")) {
                model.addAttribute("pendientes", 0);
            }
            if (!model.containsAttribute("mensajes")) {
                model.addAttribute("mensajes", 0);
            }
            if (!model.containsAttribute("noLeidos")) {
                model.addAttribute("noLeidos", 0);
            }
            if (!model.containsAttribute("nombresUsuarios")) {
                model.addAttribute("nombresUsuarios", new java.util.HashMap<>());
            }
        }
        
        // SIEMPRE retornar el template, incluso si hay errores
        return "frontend/empleado/atencion-cliente";
    }

    @GetMapping("/empleado/perfil/edit")
    public String editarPerfilEmpleado(Model model) {
        UsuarioDto usuarioAutenticado = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuarioAutenticado == null || !"empleado".equalsIgnoreCase(usuarioAutenticado.getRole())) {
            return "redirect:/login";
        }
        
        // Cargar los datos más recientes de la base de datos para asegurar que se muestren los cambios actualizados
        UsuarioDto usuario = usuarioService.usuarioPorId(usuarioAutenticado.getId())
                .orElse(usuarioAutenticado);
        
        model.addAttribute("usuario", usuario);
        return "frontend/empleado/perfil-edit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/empleado/perfil/update")
    public String actualizarPerfilEmpleado(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String phone,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String address,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String password,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String passwordConfirm,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuarioAutenticado = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuarioAutenticado == null || !"empleado".equalsIgnoreCase(usuarioAutenticado.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // Validaciones
            if (phone == null || phone.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El teléfono es obligatorio");
                return "redirect:/empleado/perfil/edit";
            }
            
            if (phone.trim().length() != 10 || !phone.trim().matches("\\d+")) {
                redirectAttributes.addFlashAttribute("error", "El teléfono debe tener 10 dígitos");
                return "redirect:/empleado/perfil/edit";
            }
            
            if (address == null || address.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La dirección es obligatoria");
                return "redirect:/empleado/perfil/edit";
            }
            
            if (address.trim().length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La dirección debe tener al menos 8 caracteres");
                return "redirect:/empleado/perfil/edit";
            }
            
            // Validar contraseña si se proporciona
            if (password != null && !password.trim().isEmpty()) {
                if (passwordConfirm == null || passwordConfirm.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe confirmar la nueva contraseña");
                    return "redirect:/empleado/perfil/edit";
                }
                
                if (!password.equals(passwordConfirm)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/empleado/perfil/edit";
                }
                
                if (password.length() < 8) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                    return "redirect:/empleado/perfil/edit";
                }
            }
            
            // Obtener el usuario completo de la BD
            UsuarioDto usuarioActual = usuarioService.usuarioPorId(usuarioAutenticado.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            // Actualizar solo los campos editables
            usuarioActual.setPhone(phone.trim());
            usuarioActual.setAddress(address.trim());
            
            // Solo actualizar contraseña si se proporcionó una nueva
            if (password != null && !password.trim().isEmpty()) {
                usuarioActual.setPassword(password.trim());
            }
            
            // Actualizar perfil usando el método correcto que actualiza phone, address y password
            UsuarioDto usuarioActualizado = usuarioService.actualizarPerfil(usuarioActual.getId(), usuarioActual);
            
            if (usuarioActualizado != null) {
                redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
            }
            redirectAttributes.addFlashAttribute("showModal", "true");
            return "redirect:/empleado/perfil/edit";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
            return "redirect:/empleado/perfil/edit";
        }
    }

    @GetMapping("/perfil/edit")
    public String editarPerfil(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuario);
        
        // Redirigir según el rol para mantener el contexto
        String role = usuario.getRole();
        if ("admin".equalsIgnoreCase(role)) {
            return "frontend/admin/perfil-edit";
        } else if ("empleado".equalsIgnoreCase(role)) {
            return "frontend/empleado/perfil-edit";
        } else if ("cliente".equalsIgnoreCase(role)) {
            return "frontend/cliente/perfil-edit";
        }
        
        return "redirect:/login";
    }

    @PostMapping("/perfil/edit")
    public String actualizarPerfil(
            @ModelAttribute UsuarioDto usuarioDto,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String forgotPassword,
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuarioAutenticado = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuarioAutenticado == null) {
            return "redirect:/login";
        }
        
        // Si no es modo "olvidé contraseña", validar contraseña actual
        if (forgotPassword == null || !"true".equals(forgotPassword)) {
            // Validar que se proporcione la contraseña actual
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Debes ingresar tu contraseña actual para realizar cambios");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return getRedirectUrl(usuarioAutenticado.getRole());
            }
            
            // Validar la contraseña actual
            boolean passwordValid = usuarioService.validarPassword(usuarioAutenticado.getId(), currentPassword);
            if (!passwordValid) {
                redirectAttributes.addFlashAttribute("mensaje", "La contraseña actual es incorrecta");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return getRedirectUrl(usuarioAutenticado.getRole());
            }
        }
        // Si es modo "olvidé contraseña", la verificación de identidad ya se hizo en el frontend
        
        // Asegurar que solo se actualice el perfil del usuario autenticado
        usuarioDto.setId(usuarioAutenticado.getId());
        usuarioDto.setRole(usuarioAutenticado.getRole()); // No permitir cambiar el rol
        
        // Si el usuario es admin o cliente, solo permitir actualizar teléfono, dirección y contraseña
        if ("admin".equalsIgnoreCase(usuarioAutenticado.getRole()) || 
            "cliente".equalsIgnoreCase(usuarioAutenticado.getRole())) {
            // Mantener los valores originales para los campos que no se pueden modificar
            usuarioDto.setName(usuarioAutenticado.getName());
            usuarioDto.setFirstName(usuarioAutenticado.getFirstName());
            usuarioDto.setLastName(usuarioAutenticado.getLastName());
            usuarioDto.setEmail(usuarioAutenticado.getEmail());
            usuarioDto.setDocumentType(usuarioAutenticado.getDocumentType());
            usuarioDto.setDocumentNumber(usuarioAutenticado.getDocumentNumber());
        }
        
        // Si la contraseña está vacía, no actualizarla
        if (usuarioDto.getPassword() == null || usuarioDto.getPassword().trim().isEmpty()) {
            usuarioDto.setPassword(null);
        }
        
        UsuarioDto usuarioActualizado = usuarioService.actualizarPerfil(usuarioAutenticado.getId(), usuarioDto);
        
        if (usuarioActualizado != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el perfil");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        return getRedirectUrl(usuarioAutenticado.getRole());
    }
    
    private String getRedirectUrl(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/perfil";
        } else if ("empleado".equalsIgnoreCase(role)) {
            return "redirect:/empleado/perfil";
        } else if ("cliente".equalsIgnoreCase(role)) {
            return "redirect:/cliente/perfil";
        }
        return "redirect:/login";
    }

    @PostMapping("/cliente/perfil/desactivar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desactivarCuenta() {
        Map<String, Object> response = new HashMap<>();
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            response.put("success", false);
            response.put("message", "Usuario no autenticado");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            boolean resultado = usuarioService.activarDesactivarUsuario(usuario.getId(), false);
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Cuenta desactivada correctamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al desactivar la cuenta");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al desactivar la cuenta: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
