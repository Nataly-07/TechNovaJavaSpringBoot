package com.technova.technov.domain.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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
import com.technova.technov.domain.entity.Compra;
import com.technova.technov.domain.entity.Venta;
import com.technova.technov.domain.entity.DetalleCompra;
import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.repository.CaracteristicaRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.CompraRepository;
import com.technova.technov.domain.repository.VentaRepository;
import com.technova.technov.domain.repository.DetalleCompraRepository;
import com.technova.technov.domain.repository.DetalleVentaRepository;
import com.technova.technov.domain.service.ProductoService;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

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
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarAvanzado(String termino, String marca, String categoria, BigDecimal precioMin, BigDecimal precioMax, String disponibilidad) {
        List<Producto> productos = productoRepository.findByEstadoTrue();
        
        return productos.stream()
                .filter(p -> {
                    // Filtro por término de búsqueda (nombre o descripción)
                    if (termino != null && !termino.trim().isEmpty()) {
                        String terminoLower = termino.toLowerCase().trim();
                        boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(terminoLower);
                        boolean matchDescripcion = p.getCaracteristica() != null && 
                                p.getCaracteristica().getDescripcion() != null &&
                                p.getCaracteristica().getDescripcion().toLowerCase().contains(terminoLower);
                        if (!matchNombre && !matchDescripcion) {
                            return false;
                        }
                    }
                    
                    // Filtro por marca
                    if (marca != null && !marca.trim().isEmpty()) {
                        if (p.getCaracteristica() == null || p.getCaracteristica().getMarca() == null ||
                                !p.getCaracteristica().getMarca().equalsIgnoreCase(marca.trim())) {
                            return false;
                        }
                    }
                    
                    // Filtro por categoría
                    if (categoria != null && !categoria.trim().isEmpty()) {
                        if (p.getCaracteristica() == null || p.getCaracteristica().getCategoria() == null ||
                                !p.getCaracteristica().getCategoria().equalsIgnoreCase(categoria.trim())) {
                            return false;
                        }
                    }
                    
                    // Filtro por rango de precio
                    if (precioMin != null || precioMax != null) {
                        if (p.getCaracteristica() == null || p.getCaracteristica().getPrecioVenta() == null) {
                            return false;
                        }
                        BigDecimal precio = p.getCaracteristica().getPrecioVenta();
                        if (precioMin != null && precio.compareTo(precioMin) < 0) {
                            return false;
                        }
                        if (precioMax != null && precio.compareTo(precioMax) > 0) {
                            return false;
                        }
                    }
                    
                    // Filtro por disponibilidad
                    if (disponibilidad != null && !disponibilidad.trim().isEmpty()) {
                        int stock = p.getStock() != null ? p.getStock() : 0;
                        if ("disponible".equalsIgnoreCase(disponibilidad.trim())) {
                            if (stock <= 0) {
                                return false;
                            }
                        } else if ("agotado".equalsIgnoreCase(disponibilidad.trim())) {
                            if (stock > 0) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                })
                .map(producto -> {
                    // Forzar la carga de la relación antes de mapear
                    if (producto.getCaracteristica() != null) {
                        producto.getCaracteristica().getCategoria(); // Forzar lazy loading
                    }
                    return convertToDto(producto);
                })
                .collect(Collectors.toList());
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

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarProductosRecientes(int cantidad) {
        // Obtener productos desde movimientos recientes (compras y ventas)
        // Primero intentar obtener desde compras recientes
        List<Producto> productosRecientes = new ArrayList<>();
        Set<Integer> productosIds = new HashSet<>();
        
        try {
            // Obtener productos desde compras recientes ordenadas por fecha
            List<Compra> comprasRecientes = 
                compraRepository.findAll().stream()
                    .sorted((c1, c2) -> {
                        if (c1.getFechaCompra() == null && c2.getFechaCompra() == null) return 0;
                        if (c1.getFechaCompra() == null) return 1;
                        if (c2.getFechaCompra() == null) return -1;
                        return c2.getFechaCompra().compareTo(c1.getFechaCompra());
                    })
                    .limit(cantidad * 2) // Obtener más para tener opciones
                    .collect(Collectors.toList());
            
            for (Compra compra : comprasRecientes) {
                if (productosIds.size() >= cantidad) break;
                List<DetalleCompra> detalles = 
                    detalleCompraRepository.findByCompra(compra);
                for (DetalleCompra detalle : detalles) {
                    Producto producto = detalle.getProducto();
                    if (producto != null && producto.getEstado() != null && producto.getEstado() 
                        && !productosIds.contains(producto.getId())) {
                        productosRecientes.add(producto);
                        productosIds.add(producto.getId());
                        if (productosIds.size() >= cantidad) break;
                    }
                }
            }
            
            // Si aún no tenemos suficientes, obtener desde ventas recientes
            if (productosIds.size() < cantidad) {
                List<Venta> ventasRecientes = 
                    ventaRepository.findAll().stream()
                        .sorted((v1, v2) -> {
                            if (v1.getFechaVenta() == null && v2.getFechaVenta() == null) return 0;
                            if (v1.getFechaVenta() == null) return 1;
                            if (v2.getFechaVenta() == null) return -1;
                            return v2.getFechaVenta().compareTo(v1.getFechaVenta());
                        })
                        .limit(cantidad * 2)
                        .collect(Collectors.toList());
                
                for (Venta venta : ventasRecientes) {
                    if (productosIds.size() >= cantidad) break;
                    List<DetalleVenta> detalles = 
                        detalleVentaRepository.findByVenta(venta);
                    for (DetalleVenta detalle : detalles) {
                        Producto producto = detalle.getProducto();
                        if (producto != null && producto.getEstado() != null && producto.getEstado() 
                            && !productosIds.contains(producto.getId())) {
                            productosRecientes.add(producto);
                            productosIds.add(producto.getId());
                            if (productosIds.size() >= cantidad) break;
                        }
                    }
                }
            }
            
            // Si aún no tenemos suficientes, completar con productos más recientes por ID
            if (productosIds.size() < cantidad) {
                Pageable pageable = PageRequest.of(0, cantidad);
                Page<Producto> productosPage = productoRepository.findByEstadoTrueOrderByIdDesc(pageable);
                for (Producto producto : productosPage.getContent()) {
                    if (productosIds.size() >= cantidad) break;
                    if (!productosIds.contains(producto.getId())) {
                        productosRecientes.add(producto);
                        productosIds.add(producto.getId());
                    }
                }
            }
            
        } catch (Exception e) {
            // Si hay error, usar método por defecto (por ID)
            Pageable pageable = PageRequest.of(0, cantidad);
            Page<Producto> productosPage = productoRepository.findByEstadoTrueOrderByIdDesc(pageable);
            productosRecientes = productosPage.getContent();
        }
        
        // Limitar a la cantidad solicitada
        productosRecientes = productosRecientes.stream()
            .limit(cantidad)
            .collect(Collectors.toList());
        
        return productosRecientes.stream()
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
