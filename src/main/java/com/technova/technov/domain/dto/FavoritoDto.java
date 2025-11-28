package com.technova.technov.domain.dto;

import lombok.*;

/**
 * DTO que representa un favorito de un usuario sobre un producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoDto {
    private Long id;
    private Long usuarioId;
    private Integer productoId;
}
