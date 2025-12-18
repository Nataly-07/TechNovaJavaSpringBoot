package com.technova.technov.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleOrdenDto {
    private Integer id;
    private Integer productoId;
    private String productoNombre; // Para vista
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
