package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una venta con su resumen e ítems asociados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDto {
    private Integer ventaId;
    private Integer usuarioId;
    private LocalDate fechaVenta;
    private BigDecimal total;
    private List<VentaItemDto> items;
}
