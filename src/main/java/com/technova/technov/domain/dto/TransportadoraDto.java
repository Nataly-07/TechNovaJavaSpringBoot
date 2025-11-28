package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportadoraDto {
    private Integer id;
    private String nombre;
    private String telefono;
    private String correo;
    private String guia;
    private BigDecimal montoEnvio;
    private Integer envioId;
}
