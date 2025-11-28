package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa una compra con su resumen e ítems asociados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraDto {
    private Integer compraId;
    private Integer usuarioId;
    private Integer proveedorId;
    private String estado;
    private LocalDateTime fechaCompra;
    private BigDecimal total;
    private List<CompraDetalleDto> items;
}
