package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.CompraDetalleDto;
import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.CompraRequestDto;
import com.technova.technov.domain.dto.CompraRequestItemDto;
import com.technova.technov.domain.entity.Compra;
import com.technova.technov.domain.entity.DetalleCompra;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Proveedor;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.CompraRepository;
import com.technova.technov.domain.repository.DetalleCompraRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.ProveedorRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.ComprasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComprasServiceImpl implements ComprasService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional
    public CompraDto crear(CompraRequestDto request) {
        Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(request.getUsuarioId()))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUsuarioId()));

        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.findByIdAndEstadoTrue(request.getProveedorId())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + request.getProveedorId()));
        }

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setProveedor(proveedor);
        compra.setEstado("procesado");
        compra.setFechaCompra(LocalDateTime.now());
        compra.setFechaDeCompra(LocalDateTime.now());
        compra.setTiempoDeEntrega(LocalDateTime.now());
        compra = compraRepository.save(compra);

        BigDecimal total = BigDecimal.ZERO;
        for (CompraRequestItemDto it : request.getItems()) {
            Producto prod = productoRepository.findByIdAndEstadoTrue(it.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + it.getProductoId()));
            // Aumentar stock con la compra
            int stock = prod.getStock() == null ? 0 : prod.getStock();
            prod.setStock(stock + it.getCantidad());
            productoRepository.save(prod);

            BigDecimal precioLinea = it.getPrecio() == null ? BigDecimal.ZERO : it.getPrecio();
            precioLinea = precioLinea.multiply(BigDecimal.valueOf(it.getCantidad()));
            total = total.add(precioLinea);

            DetalleCompra dc = new DetalleCompra();
            dc.setCompra(compra);
            dc.setProducto(prod);
            dc.setCantidad(it.getCantidad());
            dc.setPrecio(it.getPrecio());
            detalleCompraRepository.save(dc);
        }

        compra.setTotal(total);
        compraRepository.save(compra);

        return toDto(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraDto> listar() {
        return compraRepository.findAll().stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de compra descendente (más reciente primero)
                    if (a.getFechaCompra() != null && b.getFechaCompra() != null) {
                        int fechaCompare = b.getFechaCompra().compareTo(a.getFechaCompra());
                        if (fechaCompare != 0) return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompraDto detalle(Integer id) {
        return compraRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public CompraDto actualizar(Integer id, CompraRequestDto request) {
        return compraRepository.findById(id)
                .map(existing -> {
                    Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(request.getUsuarioId()))
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUsuarioId()));
                    existing.setUsuario(usuario);
                    
                    Proveedor proveedor = null;
                    if (request.getProveedorId() != null) {
                        proveedor = proveedorRepository.findByIdAndEstadoTrue(request.getProveedorId())
                                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + request.getProveedorId()));
                    }
                    existing.setProveedor(proveedor);
                    
                    // Eliminar detalles existentes
                    detalleCompraRepository.findByCompra(existing).forEach(detalleCompraRepository::delete);
                    
                    // Crear nuevos detalles
                    BigDecimal total = BigDecimal.ZERO;
                    for (CompraRequestItemDto it : request.getItems()) {
                        Producto prod = productoRepository.findByIdAndEstadoTrue(it.getProductoId())
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + it.getProductoId()));
                        
                        BigDecimal precioLinea = it.getPrecio() == null ? BigDecimal.ZERO : it.getPrecio();
                        precioLinea = precioLinea.multiply(BigDecimal.valueOf(it.getCantidad()));
                        total = total.add(precioLinea);
                        
                        DetalleCompra dc = new DetalleCompra();
                        dc.setCompra(existing);
                        dc.setProducto(prod);
                        dc.setCantidad(it.getCantidad());
                        dc.setPrecio(it.getPrecio());
                        detalleCompraRepository.save(dc);
                    }
                    
                    existing.setTotal(total);
                    Compra actualizada = compraRepository.save(existing);
                    return toDto(actualizada);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return compraRepository.findById(id)
                .map(compra -> {
                    compraRepository.delete(compra);
                    return true;
                })
                .orElse(false);
    }

    private CompraDto toDto(Compra c) {
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompra(c);
        List<CompraDetalleDto> items = detalles.stream().map(d ->
                CompraDetalleDto.builder()
                        .productoId(d.getProducto().getId())
                        .nombreProducto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precio(d.getPrecio())
                        .build()
        ).collect(Collectors.toList());
        BigDecimal total = c.getTotal() == null ? BigDecimal.ZERO : c.getTotal();
        return CompraDto.builder()
                .compraId(c.getId())
                .usuarioId(c.getUsuario() != null ? c.getUsuario().getId().intValue() : null)
                .proveedorId(c.getProveedor() != null ? c.getProveedor().getId() : null)
                .estado(c.getEstado())
                .fechaCompra(c.getFechaCompra())
                .total(total)
                .items(items)
                .build();
    }
}
