package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que resume las ventas en un período (cantidad y total vendido).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenVentasDto {
    private long cantidadVentas;
    private BigDecimal totalVendido;
}
