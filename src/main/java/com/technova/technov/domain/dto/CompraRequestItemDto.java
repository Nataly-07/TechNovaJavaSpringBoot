package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de petición que representa un ítem a comprar (producto, cantidad y precio).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraRequestItemDto {
    private Integer productoId;
    private Integer cantidad;
    private BigDecimal precio;
}
