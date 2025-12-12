package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.dto.CheckoutResponseDto;
import com.technova.technov.domain.entity.Carrito;
import com.technova.technov.domain.entity.DetalleCarrito;
import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.entity.Venta;
import com.technova.technov.domain.dto.PagoDto;
import com.technova.technov.domain.repository.CarritoRepository;
import com.technova.technov.domain.repository.DetalleCarritoRepository;
import com.technova.technov.domain.repository.DetalleVentaRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.repository.VentaRepository;
import com.technova.technov.domain.service.CheckoutService;
import com.technova.technov.domain.service.PagoService;
import com.technova.technov.domain.service.NotificacionService;
import com.technova.technov.domain.dto.NotificacionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private NotificacionService notificacionService;

    @Override
    @Transactional
    public CheckoutResponseDto checkout(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        Carrito carrito = carritoRepository.findFirstByUsuario_Id(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene carrito"));

        List<DetalleCarrito> items = detalleCarritoRepository.findByCarrito(carrito);
        if (items.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado(true); // true = activo
        venta = ventaRepository.save(venta);
        
        System.out.println("Venta creada con ID: " + venta.getId());

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCarrito dc : items) {
            Producto prod = productoRepository.findById(dc.getProducto().getId())
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado: " + dc.getProducto().getId()));
            
            // Forzar carga de característica
            if (prod.getCaracteristica() != null) {
                prod.getCaracteristica().getPrecioVenta(); // Forzar carga
            }
            
            int disponible = prod.getStock() != null ? prod.getStock() : 0;
            int cant = dc.getCantidad();
            if (disponible < cant) {
                throw new IllegalStateException("Stock insuficiente para producto ID " + prod.getId());
            }
            prod.setStock(disponible - cant);
            productoRepository.save(prod);

            BigDecimal precioUnit = BigDecimal.ZERO;
            if (prod.getCaracteristica() != null && prod.getCaracteristica().getPrecioVenta() != null) {
                precioUnit = prod.getCaracteristica().getPrecioVenta();
            } else {
                System.err.println("ADVERTENCIA: Producto " + prod.getId() + " (" + prod.getNombre() + ") no tiene precio de venta configurado");
            }
            BigDecimal precioLinea = precioUnit.multiply(BigDecimal.valueOf(cant));
            total = total.add(precioLinea);
            
            System.out.println("Checkout - Producto: " + prod.getNombre() + " x" + cant + " = $" + precioLinea);

            DetalleVenta dv = new DetalleVenta();
            dv.setVenta(venta);
            dv.setProducto(prod);
            dv.setCantidad(String.valueOf(cant));
            dv.setPrecio(precioLinea);
            detalleVentaRepository.save(dv);
        }

        detalleCarritoRepository.deleteByCarrito(carrito);

        // Crear registro de pago automáticamente asociado a la venta
        try {
            String numeroFactura = generarNumeroFactura(venta.getId());
            System.out.println("=== CHECKOUT: Creando pago para venta ID: " + venta.getId() + " ===");
            System.out.println("  -> Número de factura generado: " + numeroFactura);
            System.out.println("  -> Monto: $" + total);
            
            PagoDto pagoDto = PagoDto.builder()
                    .numeroFactura(numeroFactura)
                    .fechaPago(java.time.LocalDate.now())
                    .fechaFactura(java.time.LocalDate.now())
                    .monto(total)
                    .estadoPago("CONFIRMADO")
                    .build();
            
            PagoDto pagoCreado = pagoService.registrar(pagoDto);
            System.out.println("=== CHECKOUT: Pago creado exitosamente ===");
            System.out.println("  -> Pago ID: " + pagoCreado.getId());
            System.out.println("  -> Factura: " + pagoCreado.getNumeroFactura());
            System.out.println("  -> Monto: $" + pagoCreado.getMonto());
            System.out.println("  -> Estado: " + pagoCreado.getEstadoPago());
            System.out.println("  -> Fecha: " + pagoCreado.getFechaPago());
        } catch (Exception e) {
            System.err.println("=== ERROR: No se pudo crear el registro de pago automático ===");
            System.err.println("  -> Venta ID: " + venta.getId());
            System.err.println("  -> Error: " + e.getMessage());
            e.printStackTrace();
            // No lanzar excepción para no interrumpir el proceso de checkout
            // El pago puede ser creado manualmente después si es necesario
        }

        // Crear notificación para el cliente sobre su compra
        try {
            String productosInfo = items.stream()
                    .map(dc -> dc.getProducto().getNombre() + " (x" + dc.getCantidad() + ")")
                    .collect(Collectors.joining(", "));
            
            String mensaje = String.format(
                "¡Compra realizada exitosamente! Tu pedido #%d ha sido procesado. " +
                "Productos: %s. Total: $%.2f. " +
                "Recibirás más información sobre el envío próximamente.",
                venta.getId(),
                productosInfo.length() > 100 ? productosInfo.substring(0, 100) + "..." : productosInfo,
                total.doubleValue()
            );
            
            // Crear JSON con datos adicionales
            ObjectMapper objectMapper = new ObjectMapper();
            java.util.Map<String, Object> dataAdicional = new java.util.HashMap<>();
            dataAdicional.put("ventaId", venta.getId());
            dataAdicional.put("total", total.toString());
            dataAdicional.put("cantidadItems", items.size());
            String dataAdicionalJson = objectMapper.writeValueAsString(dataAdicional);
            
            NotificacionDto notificacion = NotificacionDto.builder()
                    .userId(usuario.getId())
                    .titulo("Compra realizada exitosamente")
                    .mensaje(mensaje)
                    .tipo("compra")
                    .icono("bx-package")
                    .leida(false)
                    .dataAdicional(dataAdicionalJson)
                    .build();
            
            notificacionService.crear(notificacion);
            System.out.println("=== NOTIFICACIÓN: Notificación de compra creada para usuario " + usuario.getId() + " ===");
        } catch (Exception e) {
            System.err.println("=== ERROR: No se pudo crear la notificación de compra ===");
            System.err.println("  -> Error: " + e.getMessage());
            e.printStackTrace();
            // No lanzar excepción para no interrumpir el proceso de checkout
        }

        List<CarritoItemDto> itemsDto = items.stream().map(dc -> CarritoItemDto.builder()
                .detalleId(dc.getId())
                .productoId(dc.getProducto().getId())
                .nombreProducto(dc.getProducto().getNombre())
                .imagen(dc.getProducto().getImagen())
                .cantidad(dc.getCantidad())
                .stock(dc.getProducto().getStock())
                .build()).collect(Collectors.toList());

        return CheckoutResponseDto.builder()
                .ventaId(venta.getId())
                .usuarioId(usuarioId)
                .total(total)
                .items(itemsDto)
                .build();
    }

    /**
     * Genera un número de factura único basado en el ID de la venta.
     * Formato: FACT-{Año}-{ID_Venta con ceros a la izquierda}
     * Ejemplo: FACT-2025-000123
     */
    private String generarNumeroFactura(Integer ventaId) {
        int año = java.time.LocalDate.now().getYear();
        // Formatear el ID de venta con ceros a la izquierda (mínimo 6 dígitos)
        String idFormateado = String.format("%06d", ventaId);
        return String.format("FACT-%d-%s", año, idFormateado);
    }
}
