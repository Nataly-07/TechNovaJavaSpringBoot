package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    @Autowired
    private SecurityUtil securityUtil;

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
            PagoService pagoService) {
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
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"cliente".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        List<com.technova.technov.domain.dto.AtencionClienteDto> tickets = new java.util.ArrayList<>();
        
        try {
            if (usuario.getId() != null) {
                tickets = atencionClienteService.listarPorUsuario(usuario.getId().intValue());
            }
        } catch (Exception e) {
            tickets = new java.util.ArrayList<>();
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("tickets", tickets);
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
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"empleado".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }
        
        // Obtener todos los productos
        List<com.technova.technov.domain.dto.ProductoDto> productos = productoService.listarProductos();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        
        return "frontend/empleado/productos";
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
            RedirectAttributes redirectAttributes) {
        
        UsuarioDto usuarioAutenticado = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuarioAutenticado == null) {
            return "redirect:/login";
        }
        
        // Asegurar que solo se actualice el perfil del usuario autenticado
        usuarioDto.setId(usuarioAutenticado.getId());
        usuarioDto.setRole(usuarioAutenticado.getRole()); // No permitir cambiar el rol
        
        // Si la contraseña está vacía, no actualizarla
        if (usuarioDto.getPassword() == null || usuarioDto.getPassword().trim().isEmpty()) {
            usuarioDto.setPassword(null);
        }
        
        UsuarioDto usuarioActualizado = usuarioService.actualizarUsuario(usuarioAutenticado.getId(), usuarioDto);
        
        if (usuarioActualizado != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el perfil");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        
        // Redirigir según el rol
        String role = usuarioAutenticado.getRole();
        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/perfil";
        } else if ("empleado".equalsIgnoreCase(role)) {
            return "redirect:/empleado/perfil";
        } else if ("cliente".equalsIgnoreCase(role)) {
            return "redirect:/cliente/perfil";
        }
        
        return "redirect:/login";
    }
}
