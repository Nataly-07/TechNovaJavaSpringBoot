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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin("*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDto>> listarTodos() {
        List<ProductoDto> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtenerPorId(@PathVariable Integer id) {
        ProductoDto producto = productoService.productoPorId(id).orElse(null);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
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
}

