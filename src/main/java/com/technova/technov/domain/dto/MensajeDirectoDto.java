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
    private Integer id;
    private String conversationId;
    private Integer parentMessageId;
    private String senderType;
    private Integer senderId;
    private Integer recipientId;
    private boolean isRead;
    private Instant readAt;
    private Integer userId;
    private String asunto;
    private String mensaje;
    private String prioridad;
    private String estado;
    private Integer empleadoId;
    private String respuesta;
    private Instant fechaRespuesta;
    private Instant createdAt;
    private Instant updatedAt;
}
