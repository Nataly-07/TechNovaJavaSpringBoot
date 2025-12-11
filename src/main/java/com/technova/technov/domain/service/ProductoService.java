package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.ProductoDto;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<ProductoDto> listarProductos();
    Optional<ProductoDto> productoPorId(Integer id);
    List<ProductoDto> porCategoria(String categoria);
    List<ProductoDto> porMarca(String marca);
    Page<ProductoDto> buscar(String termino, int page, int size);
    Page<ProductoDto> porRangoPrecio(BigDecimal min, BigDecimal max, int page, int size);
    ProductoDto crearProducto(ProductoDto productoDto);
    ProductoDto actualizarProducto(Integer id, ProductoDto productoDto);
    boolean eliminarProducto(Integer id);
    boolean activarDesactivarProducto(Integer id, boolean activar);
    List<ProductoDto> listarTodosProductos(); // Incluye activos e inactivos
    List<ProductoDto> listarProductosRecientes(int cantidad); // Obtiene los productos más recientes
    List<ProductoDto> buscarAvanzado(String termino, String marca, String categoria, BigDecimal precioMin, BigDecimal precioMax, String disponibilidad);
}
