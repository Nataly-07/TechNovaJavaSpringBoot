package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa las características de un producto para transferencia de datos.
 *
 * Incluye información de caracteristicas (categoría, marca), descripción y precios.
 */
@Data
public class CaracteristicasDto {
    private Integer id;
    private String categoria;
    private String color;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private String marca;
}
