package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa un pago registrado en el sistema.
 *
 * <p>Contiene número y fecha de factura, fecha de pago, monto y estado.</p>
 */
@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Pagos")
    private Integer id;

    @Column(name = "Fecha_Pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "Numero_Factura", nullable = false, length = 50)
    private String numeroFactura;

    @Column(name = "Fecha_Factura", nullable = false)
    private LocalDate fechaFactura;

    @Column(name = "Monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "Estado_Pago", nullable = false)
    private String estadoPago;
}
