package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa un reclamo de un cliente.
 */
@Entity
@Table(name = "reclamos")
@Data
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Reclamo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Fecha_Reclamo", nullable = false)
    private LocalDateTime fechaReclamo;

    @Column(name = "Titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "Descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "Estado", nullable = false, length = 50)
    private String estado; // "pendiente", "en_revision", "resuelto", "cerrado"

    @Column(name = "Respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "Prioridad", length = 20)
    private String prioridad; // "baja", "normal", "alta", "urgente"

    @Column(name = "Enviado_Al_Admin", nullable = false)
    private Boolean enviadoAlAdmin = false; // Indica si el reclamo fue enviado al administrador por un empleado

    @Column(name = "Evaluacion_Cliente", length = 20)
    private String evaluacionCliente; // "resuelta", "no_resuelta", null (sin evaluar)
}

