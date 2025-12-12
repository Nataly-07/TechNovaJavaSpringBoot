package com.technova.technov.domain.dto;

import lombok.*;

/**
 * DTO que representa un producto con su cantidad vendida.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoMasVendidoDto {
    private Integer productoId;
    private String nombreProducto;
    private Long cantidadVendida;
    private String imagen;
}

