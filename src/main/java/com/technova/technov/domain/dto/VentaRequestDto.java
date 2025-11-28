package com.technova.technov.domain.dto;

import lombok.*;

import java.util.List;

/**
 * DTO de petición para registrar una venta.
 *
 * Contiene el usuario y la lista de ítems a vender.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaRequestDto {
    private Integer usuarioId;        // usuario (tabla usuario)
    private List<VentaRequestItemDto> items;
}


















