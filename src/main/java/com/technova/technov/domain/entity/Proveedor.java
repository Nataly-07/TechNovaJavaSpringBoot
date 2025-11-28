package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un proveedor.
 *
 * <p>Incluye identificación y datos de contacto.</p>
 */
@Entity
@Table(name = "proveedor")
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Proveedor")
    private Integer id;

    @Column(name = "Identificacion", nullable = false, length = 50)
    private String identificacion;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Telefono", nullable = false, length = 10)
    private String telefono;

    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "Empresa")
    private String empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_producto")
    private Producto producto;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
