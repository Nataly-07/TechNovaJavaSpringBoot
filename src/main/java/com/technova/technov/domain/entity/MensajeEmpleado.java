package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mensaje_empleados")
public class MensajeEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empleado_id", nullable = false)
    private Long empleadoId;

    @Column(name = "remitente_id", nullable = false)
    private Long remitenteId;

    @Column(name = "tipo_remitente", nullable = false)
    private String tipoRemitente; // admin u otros

    @Column(name = "asunto", nullable = false)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "tipo", nullable = false)
    private String tipo; // general, instruccion, notificacion, urgencia, etc.

    @Column(name = "prioridad", nullable = false)
    private String prioridad; // normal, alta, urgente, etc.

    @Column(name = "leido", nullable = false)
    private boolean leido;

    @Column(name = "fecha_leido")
    private Instant fechaLeido;

    @Column(name = "data_adicional", columnDefinition = "LONGTEXT")
    private String dataAdicional; // JSON

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
