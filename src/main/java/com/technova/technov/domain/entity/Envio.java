package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envio")
@Data
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Envio")
    private Integer id;

    @Column(name = "Fecha_Envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "Numero_Guia", nullable = false, precision = 10, scale = 2)
    private BigDecimal numeroGuia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Ventas", nullable = false)
    private Venta venta;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
