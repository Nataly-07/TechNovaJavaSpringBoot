package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad de usuario del esquema legado (tabla usuario).
 *
 * <p>Se utiliza para relaciones históricas en módulos como ventas, compras y atención.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")
public class UsuarioLegacy {

    @Id
    @Column(name = "ID_Usuario")
    private Integer id;

    @Column(name = "Tipo_De_Documento", nullable = false)
    private String tipoDeDocumento;

    @Column(name = "Identificacion", nullable = false)
    private String identificacion;

    @Column(name = "Nombre", nullable = false)
    private String nombre;

    @Column(name = "Apellido", nullable = false)
    private String apellido;

    @Column(name = "Correo", nullable = false)
    private String correo;

    @Column(name = "Contraseña", nullable = false)
    private String contrasena;

    @Column(name = "Telefono", nullable = false)
    private String telefono;

    @Column(name = "Direccion", nullable = false)
    private String direccion;

    @Column(name = "Rol", nullable = false)
    private String rol;

    @Column(name = "Fecha_De_Registro", nullable = false)
    private LocalDate fechaDeRegistro;
}
