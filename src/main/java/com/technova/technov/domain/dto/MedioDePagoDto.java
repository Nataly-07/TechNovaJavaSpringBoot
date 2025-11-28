package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioDePagoDto {
    private Integer id;
    private String metodoPago;
    private Integer pagosId;
    private Integer detalleVentasId;
    private Integer usuarioId;
    private LocalDateTime fechaDeCompra;
    private LocalDateTime tiempoDeEntrega;
}
