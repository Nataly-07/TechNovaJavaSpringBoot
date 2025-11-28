package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una compra realizada al proveedor.
 *
 * <p>Incluye usuario, medio de pago, proveedor, importes y fechas.</p>
 */
@Entity
@Table(name = "compras")
@Data
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Compras")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Usuario", nullable = false)
    private Usuario usuario;

    // Campo comentado porque la columna ID_MedioDePago no existe en la BD
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "ID_MedioDePago")
    // private MedioDePago medioDePago;

    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "Estado")
    private String estado;

    @Column(name = "Fecha_Compra")
    private LocalDateTime fechaCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Proveedor")
    private Proveedor proveedor;

    @Column(name = "Fecha_De_Compra")
    private LocalDateTime fechaDeCompra;

    @Column(name = "Tiempo_De_Entrega")
    private LocalDateTime tiempoDeEntrega;
}
