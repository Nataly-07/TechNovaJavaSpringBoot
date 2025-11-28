package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un ítem dentro del carrito de compras.
 *
 * <p>Relaciona un {@link Carrito} con un {@link Producto} y su cantidad.</p>
 */
@Entity
@Table(name = "detallecarrito", uniqueConstraints = {
        @UniqueConstraint(name = "unique_carrito_producto", columnNames = {"ID_Carrito", "ID_Producto"})
})
@Data
public class DetalleCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DetalleCarrito")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Carrito", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Producto", nullable = false)
    private Producto producto;

    @Column(name = "Cantidad", nullable = false)
    private Integer cantidad;
}
