package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeDirectoDto {
    private Long id;
    private String conversationId;
    private Long parentMessageId;
    private String senderType;
    private Long senderId;
    private Long recipientId;
    private boolean isRead;
    private Instant readAt;
    private Long userId;
    private String asunto;
    private String mensaje;
    private String prioridad;
    private String estado;
    private Long empleadoId;
    private String respuesta;
    private Instant fechaRespuesta;
    private Instant createdAt;
    private Instant updatedAt;
}
