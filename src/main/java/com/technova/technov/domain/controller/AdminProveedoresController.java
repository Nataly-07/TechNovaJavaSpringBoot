package com.technova.technov.domain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.ProveedorDto;
import com.technova.technov.domain.service.ProveedorService;

import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de proveedores del administrador.
 */
@Controller
public class AdminProveedoresController {

    private final ProveedorService proveedorService;
    
    @Autowired
    private SecurityUtil securityUtil;

    public AdminProveedoresController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping("/admin/proveedores")
    public String listarProveedores(
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<ProveedorDto> proveedores = proveedorService.listarProveedores();
        
        // Aplicar filtro de búsqueda
        if (busqueda != null && !busqueda.isEmpty()) {
            final String busquedaLower = busqueda.toLowerCase();
            proveedores = proveedores.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(busquedaLower)) ||
                               (p.getIdentificacion() != null && p.getIdentificacion().toLowerCase().contains(busquedaLower)) ||
                               (p.getEmpresa() != null && p.getEmpresa().toLowerCase().contains(busquedaLower)) ||
                               (p.getCorreo() != null && p.getCorreo().toLowerCase().contains(busquedaLower)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("proveedores", proveedores);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalProveedores", proveedorService.listarProveedores().size());
        
        return "frontend/admin/proveedores";
    }
}

