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
public class MensajeEmpleadoDto {
    private Long id;
    private Long empleadoId;
    private Long remitenteId;
    private String tipoRemitente;
    private String asunto;
    private String mensaje;
    private String tipo;
    private String prioridad;
    private boolean leido;
    private Instant fechaLeido;
    private String dataAdicional;
    private Instant createdAt;
    private Instant updatedAt;
}
