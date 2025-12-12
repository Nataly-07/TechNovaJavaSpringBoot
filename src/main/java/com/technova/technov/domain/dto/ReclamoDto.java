package com.technova.technov.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReclamoDto {
    private Integer id;
    private Integer usuarioId;
    private String emailUsuario;
    private LocalDateTime fechaReclamo;
    private String titulo;
    private String descripcion;
    private String estado;
    private String respuesta;
    private String prioridad;
    private Boolean enviadoAlAdmin;
    private String evaluacionCliente;
}

