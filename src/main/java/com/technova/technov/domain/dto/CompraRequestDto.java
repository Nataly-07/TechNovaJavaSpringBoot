package com.technova.technov.domain.dto;

import lombok.*;

import java.util.List;

/**
 * DTO de petición para registrar una compra.
 *
 * Contiene el usuario , proveedor opcional y la lista de ítems a comprar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraRequestDto {
    private Integer usuarioId;        // usuario (tabla usuario)
    private Integer proveedorId;      // opcional
    private List<CompraRequestItemDto> items;
}
