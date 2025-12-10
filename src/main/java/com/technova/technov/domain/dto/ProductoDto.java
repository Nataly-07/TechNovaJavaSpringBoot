package com.technova.technov.domain.dto;

import lombok.*;

/**
 * DTO que representa un producto para transferencia hacia/desde la capa web.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDto {
    private Integer id;
    private String codigo;
    private String nombre;
    private String imagen;
    private Integer caracteristicasId;
    private Integer stock;
    private String proveedor;
    private Integer ingreso;
    private Integer salida;
    private CaracteristicasDto caracteristica;
    private Boolean estado; // true = activo, false = desactivado
    
    // Precios calculados para la vista (evita cálculos complejos en Thymeleaf)
    private Double precioOriginal;   // Precio con 5% de recargo (precio * 1.05)
    private Double precioDescuento;  // Precio de venta actual (precio real)
}
