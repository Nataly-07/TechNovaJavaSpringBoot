package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.DetalleOrdenDto;
import com.technova.technov.domain.dto.OrdenCompraDto;
import com.technova.technov.domain.entity.DetalleOrden;
import com.technova.technov.domain.entity.OrdenCompra;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Proveedor;
import com.technova.technov.domain.repository.DetalleOrdenRepository;
import com.technova.technov.domain.repository.OrdenCompraRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.ProveedorRepository;
import com.technova.technov.domain.service.OrdenCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDto> listarOrdenes() {
        return ordenCompraRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraDto obtenerOrdenPorId(Integer id) {
        return ordenCompraRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public OrdenCompraDto crearOrden(OrdenCompraDto ordenDto) {
        OrdenCompra orden = new OrdenCompra();
        orden.setFecha(new Date()); // Fecha actual
        orden.setEstado("PENDIENTE");
        orden.setTotal(0.0); // Se calculará con los detalles

        if (ordenDto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(ordenDto.getProveedorId())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
            orden.setProveedor(proveedor);
        }

        OrdenCompra guardada = ordenCompraRepository.save(orden);

        double totalOrden = 0.0;
        List<DetalleOrden> detallesEntidad = new ArrayList<>();

        if (ordenDto.getDetalles() != null) {
            for (DetalleOrdenDto detDto : ordenDto.getDetalles()) {
                DetalleOrden det = new DetalleOrden();
                det.setOrdenCompra(guardada);

                Producto prod = productoRepository.findById(detDto.getProductoId())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
                det.setProducto(prod);

                det.setCantidad(detDto.getCantidad());
                // Precio puede venir del DTO o del producto (compra). Asumimos que viene del
                // form o usamos el de compra
                Double precio = detDto.getPrecioUnitario();
                if (precio == null) {
                    // Fallback al precio de compra del producto si existe
                    if (prod.getCaracteristica() != null && prod.getCaracteristica().getPrecioCompra() != null) {
                        precio = prod.getCaracteristica().getPrecioCompra().doubleValue();
                    } else {
                        precio = 0.0;
                    }
                }
                det.setPrecioUnitario(precio);

                double subtotal = precio * det.getCantidad();
                det.setSubtotal(subtotal);

                totalOrden += subtotal;

                detallesEntidad.add(det);
            }
            detalleOrdenRepository.saveAll(detallesEntidad);
        }

        guardada.setDetalles(detallesEntidad);
        guardada.setTotal(totalOrden);
        return convertToDto(ordenCompraRepository.save(guardada));
    }

    @Override
    @Transactional
    public OrdenCompraDto recibirOrden(Integer id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!"PENDIENTE".equals(orden.getEstado())) {
            throw new IllegalStateException("La orden ya ha sido procesada o no está pendiente.");
        }

        orden.setEstado("RECIBIDA");

        // Actualizar stock
        if (orden.getDetalles() != null) {
            for (DetalleOrden det : orden.getDetalles()) {
                Producto prod = det.getProducto();
                // Regla clave: Si el producto ya existe, se suma la cantidad recibida al stock
                // automáticamente.
                int nuevoStock = (prod.getStock() != null ? prod.getStock() : 0) + det.getCantidad();
                prod.setStock(nuevoStock);

                // Opcional: Actualizar el ingreso
                int nuevoIngreso = (prod.getIngreso() != null ? prod.getIngreso() : 0) + det.getCantidad();
                prod.setIngreso(nuevoIngreso);

                productoRepository.save(prod);
            }
        }

        return convertToDto(ordenCompraRepository.save(orden));
    }

    private OrdenCompraDto convertToDto(OrdenCompra entidad) {
        List<DetalleOrdenDto> detallesDto = new ArrayList<>();
        if (entidad.getDetalles() != null) {
            detallesDto = entidad.getDetalles().stream()
                    .map(d -> DetalleOrdenDto.builder()
                            .id(d.getId())
                            .productoId(d.getProducto().getId())
                            .productoNombre(d.getProducto().getNombre())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotal(d.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return OrdenCompraDto.builder()
                .id(entidad.getId())
                .proveedorId(entidad.getProveedor() != null ? entidad.getProveedor().getId() : null)
                .proveedorNombre(entidad.getProveedor() != null ? entidad.getProveedor().getNombre() : "Sin Proveedor")
                .fecha(entidad.getFecha())
                .total(entidad.getTotal())
                .estado(entidad.getEstado())
                .detalles(detallesDto)
                .build();
    }
}
