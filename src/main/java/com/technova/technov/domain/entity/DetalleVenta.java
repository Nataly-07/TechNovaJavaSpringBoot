package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa el detalle (ítem) de una venta.
 *
 * <p>Incluye referencia a la {@link Venta}, el {@link Producto}, la cantidad y el precio.</p>
 */
@Entity
@Table(name = "detalleventas")
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DetalleVentas")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Ventas", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Producto", nullable = false)
    private Producto producto;

    @Column(name = "Cantidad", nullable = false, length = 100)
    private String cantidad;

    @Column(name = "Precio", nullable = false)
    private BigDecimal precio;
}
