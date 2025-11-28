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

    @Override
    @Transactional(readOnly = true)
    public List<VentaDto> listar() {
        return ventaRepository.findByEstadoTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDto detalle(Integer id) {
        return ventaRepository.findByIdAndEstadoTrue(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDto> porUsuario(Integer usuarioId) {
        return ventaRepository.findByUsuario_IdAndEstadoTrue(Long.valueOf(usuarioId)).stream().map(this::toDto).collect(Collectors.toList());
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
        Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(request.getUsuarioId()))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUsuarioId()));

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
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(String.valueOf(item.getCantidad()));
            detalle.setPrecio(item.getPrecio() == null ? BigDecimal.ZERO : item.getPrecio());
            detalleVentaRepository.save(detalle);
        }

        return toDto(venta);
    }

    @Override
    @Transactional
    public VentaDto actualizar(Integer id, VentaRequestDto request) {
        return ventaRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(request.getUsuarioId()))
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUsuarioId()));
                    existing.setUsuario(usuario);
                    
                    // Eliminar detalles existentes
                    detalleVentaRepository.findByVenta(existing).forEach(detalleVentaRepository::delete);
                    
                    // Crear nuevos detalles
                    for (VentaRequestItemDto item : request.getItems()) {
                        Producto producto = productoRepository.findByIdAndEstadoTrue(item.getProductoId())
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoId()));
                        
                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setVenta(existing);
                        detalle.setProducto(producto);
                        detalle.setCantidad(String.valueOf(item.getCantidad()));
                        detalle.setPrecio(item.getPrecio() == null ? BigDecimal.ZERO : item.getPrecio());
                        detalleVentaRepository.save(detalle);
                    }
                    
                    Venta actualizada = ventaRepository.save(existing);
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
        for (DetalleVenta dv : detalles) {
            BigDecimal precioLinea = dv.getPrecio() == null ? BigDecimal.ZERO : dv.getPrecio();
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
                .fechaVenta(v.getFechaVenta())
                .total(total)
                .items(items)
                .build();
    }
}
