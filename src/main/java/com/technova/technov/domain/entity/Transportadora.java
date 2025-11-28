package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transportadora")
@Data
public class Transportadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Transportadora")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Telefono", nullable = false, length = 100)
    private String telefono;

    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "Guia", nullable = false, length = 100)
    private String guia;

    @Column(name = "Monto_Envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Envio", nullable = false)
    private Envio envio;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
