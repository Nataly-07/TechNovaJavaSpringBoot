package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.CarritoService;
import com.technova.technov.domain.service.ComprasService;
import com.technova.technov.domain.service.FavoritoService;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.UsuarioService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

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
    
    @Autowired
    private SecurityUtil securityUtil;

    public PerfilController(
            FavoritoService favoritoService,
            CarritoService carritoService,
            ComprasService comprasService,
            UsuarioService usuarioService,
            ProductoService productoService) {
        this.favoritoService = favoritoService;
        this.carritoService = carritoService;
        this.comprasService = comprasService;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
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
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("favoritosCount", favoritosCount);
        model.addAttribute("carritoCount", carritoCount);
        model.addAttribute("comprasCount", comprasCount);
        model.addAttribute("pedidosCount", comprasCount);
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
        
        model.addAttribute("usuario", usuario);
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
        int proveedoresCount = 0; // TODO: implementar cuando exista ProveedorService
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("usersCount", usersCount);
        model.addAttribute("productosCount", productosCount);
        model.addAttribute("reportesDisponibles", reportesDisponibles);
        model.addAttribute("proveedoresCount", proveedoresCount);
        
        return "frontend/admin/perfil";
    }
}
