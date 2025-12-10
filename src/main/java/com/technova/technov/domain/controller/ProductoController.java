package com.technova.technov.domain.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.service.ProductoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin("*")
public class ProductoController {

    private final ProductoService productoService;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDto>> listarTodos() {
        List<ProductoDto> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductoDto> obtenerPorId(@PathVariable Integer id) {
        System.out.println("=== INICIO obtenerPorId - ID: " + id + " ===");
        try {
            // Para edición, necesitamos obtener el producto incluso si está inactivo
            System.out.println("Buscando producto con ID: " + id);
            java.util.Optional<com.technova.technov.domain.entity.Producto> productoOpt = productoRepository.findById(id);
            
            if (!productoOpt.isPresent()) {
                System.out.println("Producto no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Producto encontrado, mapeando a DTO...");
            com.technova.technov.domain.entity.Producto producto = productoOpt.get();
            
            // Forzar la carga de la relación antes de salir de la transacción
            if (producto.getCaracteristica() != null) {
                // Acceder a un campo para forzar el lazy loading
                producto.getCaracteristica().getCategoria();
            }
            
            ProductoDto dto = new ProductoDto();
            System.out.println("Creando ProductoDto...");
            
            dto.setEstado(producto.getEstado());
            dto.setId(producto.getId());
            dto.setCodigo(producto.getCodigo() != null ? producto.getCodigo() : "");
            dto.setNombre(producto.getNombre() != null ? producto.getNombre() : "");
            dto.setStock(producto.getStock() != null ? producto.getStock() : 0);
            dto.setProveedor(producto.getProveedor() != null ? producto.getProveedor() : "");
            dto.setImagen(producto.getImagen() != null ? producto.getImagen() : "");
            
            System.out.println("Datos básicos del producto asignados");
            
            if (producto.getCaracteristica() != null) {
                System.out.println("Producto tiene características, mapeando...");
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
                System.out.println("Características mapeadas correctamente");
            } else {
                System.out.println("Producto NO tiene características, creando DTO vacío");
                dto.setCaracteristica(new CaracteristicasDto());
            }
            
            System.out.println("=== FIN obtenerPorId - Retornando DTO ===");
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            System.err.println("=== ERROR en obtenerPorId ===");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoDto>> porCategoria(@PathVariable String categoria) {
        List<ProductoDto> productos = productoService.porCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<ProductoDto>> porMarca(@PathVariable String marca) {
        List<ProductoDto> productos = productoService.porMarca(marca);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<ProductoDto>> buscar(
            @RequestParam String termino,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductoDto> productos = productoService.buscar(termino, page, size);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/precio")
    public ResponseEntity<Page<ProductoDto>> porRangoPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductoDto> productos = productoService.porRangoPrecio(min, max, page, size);
        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<ProductoDto> crear(@RequestBody ProductoDto productoDto) {
        ProductoDto creado = productoService.crearProducto(productoDto);
        return ResponseEntity.ok(creado);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDto> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoDto productoDto) {
        ProductoDto productoActualizado = productoService.actualizarProducto(id, productoDto);
        if (productoActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productoActualizado);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        boolean eliminarProducto = productoService.eliminarProducto(id);
        if (!eliminarProducto) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<ProductoDto> activarDesactivarProducto(
            @PathVariable Integer id,
            @RequestParam boolean activar) {
        try {
            System.out.println("=== INICIO activarDesactivarProducto - ID: " + id + ", activar: " + activar + " ===");
            
            // Cambiar el estado del producto
            boolean resultado = productoService.activarDesactivarProducto(id, activar);
            if (!resultado) {
                System.out.println("No se pudo cambiar el estado del producto");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Estado cambiado correctamente, obteniendo producto actualizado...");
            // Obtener el producto actualizado (puede estar inactivo)
            java.util.Optional<com.technova.technov.domain.entity.Producto> productoOpt = productoRepository.findById(id);
            
            if (!productoOpt.isPresent()) {
                System.out.println("Producto no encontrado después de cambiar estado");
                return ResponseEntity.notFound().build();
            }
            
            com.technova.technov.domain.entity.Producto producto = productoOpt.get();
            
            // Forzar la carga de la relación antes de salir de la transacción
            if (producto.getCaracteristica() != null) {
                producto.getCaracteristica().getCategoria();
            }
            
            ProductoDto dto = new ProductoDto();
            dto.setEstado(producto.getEstado());
            dto.setId(producto.getId());
            dto.setCodigo(producto.getCodigo() != null ? producto.getCodigo() : "");
            dto.setNombre(producto.getNombre() != null ? producto.getNombre() : "");
            dto.setStock(producto.getStock() != null ? producto.getStock() : 0);
            dto.setProveedor(producto.getProveedor() != null ? producto.getProveedor() : "");
            dto.setImagen(producto.getImagen() != null ? producto.getImagen() : "");
            
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
            
            System.out.println("=== FIN activarDesactivarProducto - Retornando DTO con estado: " + dto.getEstado() + " ===");
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            System.err.println("=== ERROR en activarDesactivarProducto ===");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}

