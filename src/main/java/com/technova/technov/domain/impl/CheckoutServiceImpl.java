package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.dto.CheckoutResponseDto;
import com.technova.technov.domain.entity.Carrito;
import com.technova.technov.domain.entity.DetalleCarrito;
import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.entity.Venta;
import com.technova.technov.domain.repository.CarritoRepository;
import com.technova.technov.domain.repository.DetalleCarritoRepository;
import com.technova.technov.domain.repository.DetalleVentaRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.repository.VentaRepository;
import com.technova.technov.domain.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
