package com.technova.technov.domain.dto;

import lombok.*;

/**
 * DTO que representa un proveedor (identificación y datos de contacto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorDto {
    private Integer id;
    private String identificacion;
    private String nombre;
    private String telefono;
    private String correo;
    private String empresa;
    private Integer productoId;
    private Boolean estado;
}
