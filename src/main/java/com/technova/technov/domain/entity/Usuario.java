package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un usuario actual del sistema (tabla users).
 *
 * <p>Incluye datos de autenticación y perfil (nombre, correo, documento, contacto y rol).</p>
 */
@Entity
@Table(name = "users")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
