package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDto {
    private Integer id;
    private LocalDate fechaPago;
    private String numeroFactura;
    private LocalDate fechaFactura;
    private BigDecimal monto;
    private String estadoPago;
}
