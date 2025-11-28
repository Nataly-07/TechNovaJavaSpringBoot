package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que agrupa características técnicas de un producto.
 *
 * incluye atributos de
 * clasificación (categoría, marca), descripción y precios.</p>
 */
@Entity
@Table(name = "caracteristicas")
@Data
public class Caracteristica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Caracteristicas")
    private Integer id;

    @Column(name = "Categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "Color", nullable = false, length = 100)
    private String color;

    @Column(name = "Descripcion")
    private String descripcion;

    @Column(name = "Precio_Compra", nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "Precio_Venta", nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "Marca", nullable = false, length = 100)
    private String marca;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
