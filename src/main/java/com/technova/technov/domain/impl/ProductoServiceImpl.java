package com.technova.technov.domain.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.entity.Caracteristica;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.repository.CaracteristicaRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.service.ProductoService;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarProductos() {
        List<Producto> productos = productoRepository.findByEstadoTrue();
        return productos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDto> productoPorId(Integer id) {
        return productoRepository.findByIdAndEstadoTrue(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> porCategoria(String categoria) {
        return productoRepository.findByCaracteristica_CategoriaIgnoreCaseAndEstadoTrue(categoria)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> porMarca(String marca) {
        return productoRepository.findByCaracteristica_MarcaIgnoreCaseAndEstadoTrue(marca)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDto> buscar(String termino, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        String q = termino == null ? "" : termino.trim();
        return productoRepository
                .buscarProductosNoEliminados(q, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDto> porRangoPrecio(BigDecimal min, BigDecimal max, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        BigDecimal minimo = min == null ? BigDecimal.ZERO : min;
        BigDecimal maximo = max == null ? new BigDecimal("999999999") : max;
        return productoRepository.findByCaracteristica_PrecioVentaBetweenAndEstadoTrue(minimo, maximo, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public ProductoDto crearProducto(ProductoDto productoDto) {
        Producto producto = modelMapper.map(productoDto, Producto.class);
        producto.setId(null);
        producto.setEstado(true); // true = activo
        
        if (productoDto.getCaracteristicasId() != null) {
            Caracteristica caracteristica = caracteristicaRepository.findByIdAndEstadoTrue(productoDto.getCaracteristicasId())
                    .orElseThrow(() -> new IllegalArgumentException("Característica no encontrada: " + productoDto.getCaracteristicasId()));
            producto.setCaracteristica(caracteristica);
        }
        
        Producto guardado = productoRepository.save(producto);
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public ProductoDto actualizarProducto(Integer id, ProductoDto productoDto) {
        return productoRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    existing.setNombre(productoDto.getNombre());
                    existing.setStock(productoDto.getStock());
                    
                    if (productoDto.getCaracteristicasId() != null) {
                        Caracteristica caracteristica = caracteristicaRepository.findByIdAndEstadoTrue(productoDto.getCaracteristicasId())
                                .orElseThrow(() -> new IllegalArgumentException("Característica no encontrada: " + productoDto.getCaracteristicasId()));
                        existing.setCaracteristica(caracteristica);
                    }
                    
                    Producto actualizado = productoRepository.save(existing);
                    return convertToDto(actualizado);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminarProducto(Integer id) {
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setEstado(false); 
                    productoRepository.save(producto);
                    return true;
                })
                .orElse(false);
    }

    private ProductoDto convertToDto(Producto producto) {
        ProductoDto dto = modelMapper.map(producto, ProductoDto.class);
        if (producto.getCaracteristica() != null) {
            dto.setCaracteristicasId(producto.getCaracteristica().getId());
            dto.setCaracteristica(modelMapper.map(producto.getCaracteristica(), CaracteristicasDto.class));
        }
        return dto;
    }
}
