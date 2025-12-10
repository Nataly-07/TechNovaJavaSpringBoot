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
        return productoRepository.findById(id) // Buscar por ID sin filtrar por estado para poder editar productos inactivos
                .map(existing -> {
                    existing.setNombre(productoDto.getNombre());
                    existing.setStock(productoDto.getStock());
                    existing.setCodigo(productoDto.getCodigo());
                    existing.setProveedor(productoDto.getProveedor() != null ? productoDto.getProveedor() : "");
                    existing.setImagen(productoDto.getImagen() != null ? productoDto.getImagen() : "");
                    
                    // Actualizar ingreso y salida
                    if (productoDto.getIngreso() != null) {
                        existing.setIngreso(productoDto.getIngreso());
                    }
                    if (productoDto.getSalida() != null) {
                        existing.setSalida(productoDto.getSalida());
                    }
                    
                    if (productoDto.getCaracteristicasId() != null) {
                        Caracteristica caracteristica = caracteristicaRepository.findById(productoDto.getCaracteristicasId())
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

    @Override
    @Transactional
    public boolean activarDesactivarProducto(Integer id, boolean activar) {
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setEstado(activar); 
                    productoRepository.save(producto);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarTodosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(producto -> {
                    // Forzar la carga de la relación antes de mapear
                    if (producto.getCaracteristica() != null) {
                        producto.getCaracteristica().getCategoria(); // Forzar lazy loading
                    }
                    return convertToDto(producto);
                })
                .collect(Collectors.toList());
    }

    private ProductoDto convertToDto(Producto producto) {
        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo() != null ? producto.getCodigo() : "");
        dto.setNombre(producto.getNombre() != null ? producto.getNombre() : "");
        dto.setImagen(producto.getImagen() != null ? producto.getImagen() : "");
        dto.setStock(producto.getStock() != null ? producto.getStock() : 0);
        dto.setProveedor(producto.getProveedor() != null ? producto.getProveedor() : "");
        dto.setIngreso(producto.getIngreso() != null ? producto.getIngreso() : 0);
        dto.setSalida(producto.getSalida() != null ? producto.getSalida() : 0);
        dto.setEstado(producto.getEstado() != null ? producto.getEstado() : true);
        
        if (producto.getCaracteristica() != null) {
            dto.setCaracteristicasId(producto.getCaracteristica().getId());
            CaracteristicasDto caracteristicaDto = new CaracteristicasDto();
            caracteristicaDto.setId(producto.getCaracteristica().getId());
            caracteristicaDto.setCategoria(producto.getCaracteristica().getCategoria() != null ? producto.getCaracteristica().getCategoria() : "");
            caracteristicaDto.setMarca(producto.getCaracteristica().getMarca() != null ? producto.getCaracteristica().getMarca() : "");
            caracteristicaDto.setColor(producto.getCaracteristica().getColor() != null ? producto.getCaracteristica().getColor() : "");
            caracteristicaDto.setDescripcion(producto.getCaracteristica().getDescripcion() != null ? producto.getCaracteristica().getDescripcion() : "");
            caracteristicaDto.setPrecioCompra(producto.getCaracteristica().getPrecioCompra());
            caracteristicaDto.setPrecioVenta(producto.getCaracteristica().getPrecioVenta());
            dto.setCaracteristica(caracteristicaDto);
        } else {
            dto.setCaracteristica(new CaracteristicasDto());
        }
        
        return dto;
    }
}
