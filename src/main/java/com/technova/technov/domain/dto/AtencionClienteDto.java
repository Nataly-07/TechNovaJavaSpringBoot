package com.technova.technov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionClienteDto {
    private Integer id;
    private Integer usuarioId;
    private String emailUsuario;
    private LocalDateTime fechaConsulta;
    private String tema;
    private String descripcion;
    private String estado;
    private String respuesta;
}
