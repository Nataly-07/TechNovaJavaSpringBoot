package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una solicitud de atención al cliente.
 *
 *  realiza la consulta, el tema, la descripción, el estado y la respuesta.
 */
@Entity
@Table(name = "atencioncliente")
@Data
public class AtencionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Atencion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Fecha_Consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(name = "Tema", nullable = false, length = 150)
    private String tema;

    @Column(name = "Descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "Estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "Respuesta", columnDefinition = "TEXT")
    private String respuesta;
}
