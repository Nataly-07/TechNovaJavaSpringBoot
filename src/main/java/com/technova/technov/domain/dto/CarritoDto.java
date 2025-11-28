package com.technova.technov.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO que representa el resumen de un carrito de compras.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDto {
    private Integer id;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private String estado;
}
