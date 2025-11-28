package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta del proceso de checkout con el resumen de la venta generada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponseDto {
    private Integer ventaId;
    private Integer usuarioId;
    private BigDecimal total;
    private List<CarritoItemDto> items;
}
