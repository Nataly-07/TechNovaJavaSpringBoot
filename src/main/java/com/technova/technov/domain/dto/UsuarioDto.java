package com.technova.technov.domain.dto;

import lombok.*;

import java.io.Serializable;

/**
 * DTO que representa un usuario del sistema (datos básicos de perfil y contacto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private String phone;
    private String address;
    private String role;
    private Boolean estado;
}
