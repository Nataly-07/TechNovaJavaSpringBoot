package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Orden")
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Producto", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Producto producto;

    @Column(name = "Cantidad")
    private Integer cantidad;

    @Column(name = "PrecioUnitario")
    private Double precioUnitario;

    @Column(name = "Subtotal")
    private Double subtotal;
}
