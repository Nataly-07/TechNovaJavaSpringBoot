package com.technova.technov.domain.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.ResumenVentasDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.dto.VentaItemDto;
import com.technova.technov.domain.dto.VentaRequestDto;
import com.technova.technov.domain.dto.VentaRequestItemDto;
import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.entity.Venta;
import com.technova.technov.domain.repository.DetalleVentaRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.repository.VentaRepository;
import com.technova.technov.domain.service.NotificacionService;
import com.technova.technov.domain.service.VentaService;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Override
    @Transactional(readOnly = true)
    public List<VentaDto> listar() {
        return ventaRepository.findByEstadoTrue().stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha descendente (más reciente primero)
                    int fechaCompare = b.getFechaVenta().compareTo(a.getFechaVenta());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    // Si las fechas son iguales, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDto detalle(Integer id) {
        return ventaRepository.findByIdAndEstadoTrue(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public List<VentaDto> porUsuario(Integer usuarioId) {
        return ventaRepository.findByUsuario_IdAndEstadoTrue(usuarioId).stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha descendente (más reciente primero)
                    int fechaCompare = b.getFechaVenta().compareTo(a.getFechaVenta());
                    if (fechaCompare != 0)
                        return fechaCompare;
                    // Si las fechas son iguales, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenVentasDto resumen(LocalDate desde, LocalDate hasta) {
        List<Venta> ventas = ventaRepository.findByFechaVentaBetweenAndEstadoTrue(desde, hasta);
        BigDecimal total = BigDecimal.ZERO;
        long count = 0;
        for (Venta v : ventas) {
            count++;
            for (DetalleVenta dv : detalleVentaRepository.findByVenta(v)) {
                total = total.add(dv.getPrecio() == null ? BigDecimal.ZERO : dv.getPrecio());
            }
        }
        return ResumenVentasDto.builder()
                .cantidadVentas(count)
                .totalVendido(total)
                .build();
    }

    @Override
    @Transactional
    public VentaDto crear(VentaRequestDto request) {
        boolean esPuntoFisico = Boolean.TRUE.equals(request.getPuntoFisico());
        Integer idUsuarioVenta = request.getUsuarioId();
        Integer empleadoId = request.getEmpleadoId();
        Usuario usuario;

        if (esPuntoFisico) {
            Integer idEmpleado = empleadoId != null ? empleadoId : idUsuarioVenta;
            if (idEmpleado == null) {
                throw new IllegalArgumentException("Empleado requerido para registrar venta en punto físico");
            }
            usuario = usuarioRepository.findByIdAndEstadoTrue(idEmpleado)
                    .filter(u -> u.getRole() != null && "empleado".equalsIgnoreCase(u.getRole()))
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado para venta en punto físico"));
        } else {
            usuario = usuarioRepository.findByIdAndEstadoTrue(idUsuarioVenta)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuarioVenta));
        }

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado(true); // true = activo
        venta = ventaRepository.save(venta);

        for (VentaRequestItemDto item : request.getItems()) {
            Producto producto = productoRepository.findByIdAndEstadoTrue(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoId()));

            // Reducir stock con la venta
            int stock = producto.getStock() == null ? 0 : producto.getStock();
            if (stock < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setStock(stock - item.getCantidad());
            Producto guardado = productoRepository.save(producto);

            // Notificar si el producto se agota
            if (guardado.getStock() != null && guardado.getStock() == 0) {
                notificacionService.crearNotificacionRol(
                        "EMPLEADO",
                        "Producto Agotado",
                        "El producto '" + guardado.getNombre() + "' se ha quedado sin stock.",
                        "Visualización de Artículos",
                        "bx-error-circle");

                notificacionService.crearNotificacionSistema(
                        "Producto Agotado",
                        "El producto '" + guardado.getNombre() + "' se ha quedado sin stock.",
                        "Visualización de Artículos",
                        "bx-error-circle");
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(String.valueOf(item.getCantidad()));
            BigDecimal precioLinea = item.getPrecio();
            if (precioLinea == null && producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null) {
                precioLinea = producto.getCaracteristica().getPrecioVenta().multiply(BigDecimal.valueOf(item.getCantidad()));
            }
            detalle.setPrecio(precioLinea == null ? BigDecimal.ZERO : precioLinea);
            detalleVentaRepository.save(detalle);
        }

        VentaDto ventaDto = toDto(venta);

        if (esPuntoFisico) {
            notificacionService.crearNotificacionSistema(
                    "Venta en Punto Físico Registrada",
                    "Venta #" + venta.getId() + " registrada por el empleado " + usuario.getName() + " por $"
                            + ventaDto.getTotal(),
                    "Pedidos",
                    "bx-store");

            notificacionService.crearNotificacionRol(
                    "EMPLEADO",
                    "Nueva Venta en Punto Físico #" + venta.getId(),
                    "El empleado " + usuario.getName() + " registró una venta presencial por $" + ventaDto.getTotal(),
                    "Pedidos",
                    "bx-store");
        } else {
            // Notificación de sistema
            notificacionService.crearNotificacionSistema(
                    "Nuevo Pedido Registrado",
                    "Se ha realizado el pedido #" + venta.getId() + " por el usuario " + usuario.getEmail() + ". Total: $"
                            + ventaDto.getTotal(),
                    "Pedidos",
                    "bx-shopping-bag");

            // Notificación al Cliente
            notificacionService.crear(
                    usuario,
                    "Pedido Realizado con Éxito",
                    "Has realizado el pedido #" + venta.getId() + " por un total de $" + ventaDto.getTotal(),
                    "Pedidos",
                    "bx-shopping-bag");

            // Notificación al Empleado (Pedidos)
            notificacionService.crearNotificacionRol(
                    "EMPLEADO",
                    "Nuevo Pedido #" + venta.getId(),
                    "Cliente " + usuario.getEmail() + " realizó un pedido por $" + ventaDto.getTotal(),
                    "Pedidos",
                    "bx-shopping-bag");
        }

        return ventaDto;
    }

    @Override
    @Transactional
    public VentaDto actualizar(Integer id, VentaRequestDto request) {
        return ventaRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(request.getUsuarioId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Usuario no encontrado: " + request.getUsuarioId()));
                    existing.setUsuario(usuario);

                    // Eliminar detalles existentes
                    detalleVentaRepository.findByVenta(existing).forEach(detalleVentaRepository::delete);

                    // Crear nuevos detalles
                    for (VentaRequestItemDto item : request.getItems()) {
                        Producto producto = productoRepository.findByIdAndEstadoTrue(item.getProductoId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Producto no encontrado: " + item.getProductoId()));

                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setVenta(existing);
                        detalle.setProducto(producto);
                        detalle.setCantidad(String.valueOf(item.getCantidad()));
                        detalle.setPrecio(item.getPrecio() == null ? BigDecimal.ZERO : item.getPrecio());
                        detalleVentaRepository.save(detalle);
                    }

                    Venta actualizada = ventaRepository.save(existing);

                    // Notificación al Cliente
                    notificacionService.crear(
                            usuario,
                            "Pedido Actualizado",
                            "Tu pedido #" + existing.getId() + " ha sido actualizado.",
                            "Pedidos",
                            "bx-edit");

                    // Notificación al Empleado (Pedidos)
                    notificacionService.crearNotificacionRol(
                            "EMPLEADO",
                            "Pedido #" + existing.getId() + " Actualizado",
                            "El pedido ha cambiado de estado o detalles.",
                            "Pedidos",
                            "bx-edit");

                    return toDto(actualizada);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return ventaRepository.findById(id)
                .map(venta -> {
                    venta.setEstado(false);
                    ventaRepository.save(venta);
                    return true;
                })
                .orElse(false);
    }

    private VentaDto toDto(Venta v) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(v);
        List<VentaItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean necesitaActualizacion = false;

        for (DetalleVenta dv : detalles) {
            BigDecimal precioLinea = dv.getPrecio() == null ? BigDecimal.ZERO : dv.getPrecio();

            // Si el precio está en 0, intentar recalcularlo desde las características del
            // producto
            if (precioLinea.compareTo(BigDecimal.ZERO) == 0) {
                Producto producto = productoRepository.findById(dv.getProducto().getId()).orElse(null);
                if (producto != null && producto.getCaracteristica() != null
                        && producto.getCaracteristica().getPrecioVenta() != null) {
                    BigDecimal precioUnitario = producto.getCaracteristica().getPrecioVenta();
                    int cantidad = Integer.valueOf(dv.getCantidad());
                    precioLinea = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

                    // Actualizar el precio en la base de datos
                    dv.setPrecio(precioLinea);
                    detalleVentaRepository.save(dv);
                    necesitaActualizacion = true;

                    System.out.println("Precio recalculado para DetalleVenta #" + dv.getId() +
                            " - Producto: " + producto.getNombre() +
                            " x" + cantidad + " = $" + precioLinea);
                }
            }

            total = total.add(precioLinea);
            items.add(VentaItemDto.builder()
                    .productoId(dv.getProducto().getId())
                    .nombreProducto(dv.getProducto().getNombre())
                    .cantidad(Integer.valueOf(dv.getCantidad()))
                    .precioLinea(precioLinea)
                    .build());
        }

        return VentaDto.builder()
                .ventaId(v.getId())
                .usuarioId(v.getUsuario().getId().intValue())
                .empleadoId(v.getUsuario().getRole() != null && "empleado".equalsIgnoreCase(v.getUsuario().getRole())
                        ? v.getUsuario().getId()
                        : null)
                .empleadoNombre(v.getUsuario().getRole() != null && "empleado".equalsIgnoreCase(v.getUsuario().getRole())
                        ? v.getUsuario().getName()
                        : null)
                .tipoVenta(v.getUsuario().getRole() != null && "empleado".equalsIgnoreCase(v.getUsuario().getRole())
                        ? "PUNTO_FISICO"
                        : "ONLINE")
                .fechaVenta(v.getFechaVenta())
                .total(total)
                .items(items)
                .build();
    }
}
