package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.OrdenCompraDto;
import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.ProveedorDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.OrdenCompraService;
import com.technova.technov.domain.service.ProductoService;
import com.technova.technov.domain.service.ProveedorService;
import com.technova.technov.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/ordenes")
public class OrdenCompraController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping
    public String listarOrdenes(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<OrdenCompraDto> ordenes = ordenCompraService.listarOrdenes();
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("usuario", usuario);
        return "frontend/admin/ordenes/lista"; // Create this view
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        UsuarioDto usuario = securityUtil.getUsuarioAutenticado().orElse(null);
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRole())) {
            return "redirect:/login";
        }

        List<ProveedorDto> proveedores = proveedorService.listarProveedores();
        List<ProductoDto> productos = productoService.listarProductos();

        OrdenCompraDto ordenDto = new OrdenCompraDto();
        model.addAttribute("orden", ordenDto);
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("productos", productos);
        model.addAttribute("usuario", usuario);

        return "frontend/admin/ordenes/form"; // Create this view
    }

    @PostMapping("/guardar")
    public String guardarOrden(@ModelAttribute OrdenCompraDto ordenDto, RedirectAttributes redirectAttributes) {
        try {
            // Eliminar detalles vacíos o con cantidad 0
            if (ordenDto.getDetalles() != null) {
                ordenDto.getDetalles()
                        .removeIf(d -> d.getProductoId() == null || d.getCantidad() == null || d.getCantidad() <= 0);
            }

            if (ordenDto.getDetalles() == null || ordenDto.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe agregar al menos un producto a la orden.");
                return "redirect:/admin/ordenes/crear";
            }

            ordenCompraService.crearOrden(ordenDto);
            redirectAttributes.addFlashAttribute("mensaje", "Orden creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la orden: " + e.getMessage());
            return "redirect:/admin/ordenes/crear";
        }
        return "redirect:/admin/ordenes";
    }

    @PostMapping("/recibir/{id}")
    public String recibirOrden(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ordenCompraService.recibirOrden(id);
            redirectAttributes.addFlashAttribute("mensaje", "Orden recibida y stock actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al recibir la orden: " + e.getMessage());
        }
        return "redirect:/admin/ordenes";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<OrdenCompraDto> obtenerDetalleOrden(@PathVariable Integer id) {
        OrdenCompraDto orden = ordenCompraService.obtenerOrdenPorId(id);
        if (orden != null) {
            return org.springframework.http.ResponseEntity.ok(orden);
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}
