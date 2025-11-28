package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvioDto {
    private Integer id;
    private LocalDateTime fechaEnvio;
    private BigDecimal numeroGuia;
    private Integer ventaId;
}
