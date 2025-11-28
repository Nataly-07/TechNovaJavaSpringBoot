package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem (detalle) dentro de una venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaItemDto {
    private Integer productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioLinea;
}
