package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa las ventas de un mes específico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaPorMesDto {
    private String mes; // Ej: "Enero", "Febrero"
    private int anio;
    private BigDecimal total;
    private long cantidad;
}

