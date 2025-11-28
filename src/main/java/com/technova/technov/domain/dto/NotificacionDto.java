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
public class NotificacionDto {
    private Long id;
    private Long userId;
    private String titulo;
    private String mensaje;
    private String tipo;
    private String icono;
    private boolean leida;
    private String dataAdicional;
    private Instant fechaCreacion;
    private Instant createdAt;
    private Instant updatedAt;
}
