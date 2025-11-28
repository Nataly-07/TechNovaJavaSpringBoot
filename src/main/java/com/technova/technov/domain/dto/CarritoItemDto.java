package com.technova.technov.domain.dto;

import lombok.*;

/**
 * DTO que representa un ítem dentro del carrito de compras.
 *
 * Contiene informacion , nombre, imagen, cantidad y stock del producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItemDto {
    private Integer detalleId;
    private Integer productoId;
    private String nombreProducto;
    private String imagen;
    private Integer cantidad;
    private Integer stock;
}
