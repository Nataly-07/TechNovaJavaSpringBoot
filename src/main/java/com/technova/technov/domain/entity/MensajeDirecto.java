package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mensaje_directos")
public class MensajeDirecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "sender_type", nullable = false)
    private String senderType; // cliente | empleado

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asunto", nullable = false)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "prioridad", nullable = false)
    private String prioridad; // normal | alta | urgente, etc.

    @Column(name = "estado", nullable = false)
    private String estado; // enviado | leido | respondido, etc.

    @Column(name = "empleado_id")
    private Long empleadoId;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "fecha_respuesta")
    private Instant fechaRespuesta;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
