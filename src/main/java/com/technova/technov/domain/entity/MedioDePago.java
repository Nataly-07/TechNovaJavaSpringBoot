package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un medio de pago asociado a pago, detalle de venta y usuario.
 *
 * <p>Incluye método de pago y marcas de tiempo relacionadas a la compra/entrega.</p>
 * <p>Nota: Esta entidad tiene relaciones con Pago, DetalleVenta y Usuario.</p>
 */
@Entity
@Table(name = "mediodepago")
@Data
public class MedioDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MedioDePago")
    private Integer id;

    @Column(name = "Metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Pagos", nullable = false)
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DetalleVentas", nullable = false)
    private DetalleVenta detalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Fecha_De_Compra")
    private LocalDateTime fechaDeCompra;

    @Column(name = "Tiempo_De_Entrega")
    private LocalDateTime tiempoDeEntrega;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
