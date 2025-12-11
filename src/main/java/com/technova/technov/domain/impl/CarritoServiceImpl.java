package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.CarritoItemDto;
import com.technova.technov.domain.entity.*;
import com.technova.technov.domain.repository.*;
import com.technova.technov.domain.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    @Override
    public List<CarritoItemDto> listar(Integer usuarioId) {
        Optional<Carrito> carritoOpt = carritoRepository.findFirstByUsuario_Id(Long.valueOf(usuarioId));
        if (carritoOpt.isEmpty()) {
            return List.of(); // Retornar lista vacía si no existe carrito
        }
        return detalleCarritoRepository.findByCarrito(carritoOpt.get()).stream()
                .sorted((a, b) -> {
                    // Ordenar por ID descendente (más reciente primero)
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<CarritoItemDto> agregar(Integer usuarioId, Integer productoId, Integer cantidad) {
        if (cantidad == null || cantidad < 1) cantidad = 1;
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        
        // Buscar el producto y verificar stock
        Producto prod = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        
        // Verificar si el producto está agotado
        if (prod.getStock() == null || prod.getStock() <= 0) {
            throw new IllegalArgumentException("El producto está agotado y no se puede agregar al carrito");
        }
        
        // Buscar si ya existe item del mismo producto
        List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarrito(carrito);
        for (DetalleCarrito d : detalles) {
            if (d.getProducto().getId().equals(productoId)) {
                // Verificar stock antes de incrementar cantidad
                if (prod.getStock() < (d.getCantidad() + cantidad)) {
                    throw new IllegalArgumentException("No hay suficiente stock disponible");
                }
                d.setCantidad(d.getCantidad() + cantidad);
                detalleCarritoRepository.save(d);
                return listar(usuarioId);
            }
        }
        
        // Verificar stock antes de agregar nuevo item
        if (prod.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }
        
        DetalleCarrito nuevo = new DetalleCarrito();
        nuevo.setId(null);
        nuevo.setCarrito(carrito);
        nuevo.setProducto(prod);
        nuevo.setCantidad(cantidad);
        detalleCarritoRepository.save(nuevo);
        return listar(usuarioId);
    }

    @Transactional
    @Override
    public List<CarritoItemDto> actualizar(Integer usuarioId, Integer detalleId, Integer cantidad) {
        if (cantidad == null || cantidad < 1) cantidad = 1;
        DetalleCarrito detalle = detalleCarritoRepository.findById(detalleId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado: " + detalleId));
        detalle.setCantidad(cantidad);
        detalleCarritoRepository.save(detalle);
        return listar(usuarioId);
    }

    @Transactional
    @Override
    public List<CarritoItemDto> eliminar(Integer usuarioId, Integer detalleId) {
        detalleCarritoRepository.deleteById(detalleId);
        return listar(usuarioId);
    }

    @Transactional
    @Override
    public void vaciar(Integer usuarioId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        detalleCarritoRepository.deleteByCarrito(carrito);
    }

    private Carrito obtenerOCrearCarrito(Integer usuarioId) {
        Optional<Carrito> existente = carritoRepository.findFirstByUsuario_Id(Long.valueOf(usuarioId));
        if (existente.isPresent()) return existente.get();
        Usuario usuario = usuarioRepository.findById(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        Carrito nuevo = new Carrito();
        nuevo.setId(null);
        nuevo.setUsuario(usuario);
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setEstado("activo");
        return carritoRepository.save(nuevo);
    }

    private CarritoItemDto toDto(DetalleCarrito d) {
        return CarritoItemDto.builder()
                .detalleId(d.getId())
                .productoId(d.getProducto().getId())
                .nombreProducto(d.getProducto().getNombre())
                .imagen(d.getProducto().getImagen())
                .cantidad(d.getCantidad())
                .stock(d.getProducto().getStock())
                .build();
    }
}
