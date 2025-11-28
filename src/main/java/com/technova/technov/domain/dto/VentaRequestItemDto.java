package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem dentro de una petición de venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaRequestItemDto {
    private Integer productoId;
    private Integer cantidad;
    private BigDecimal precio;
}


















