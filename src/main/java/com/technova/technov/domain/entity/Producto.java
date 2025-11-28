package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un producto del catálogo.
 *
 * <p>Mapea la tabla {@code producto} e incluye referencias a sus
 * características técnicas mediante la relación con {@link Caracteristica}.</p>
 */
@Entity
@Table(name = "producto")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Producto")
    private Integer id;

    @Column(name = "Codigo", nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Imagen")
    private String imagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Caracteristicas")
    private Caracteristica caracteristica;

    @Column(name = "Stock", nullable = false)
    private Integer stock;

    @Column(name = "Proveedor")
    private String proveedor; // En SQL está como texto; existe tabla proveedor aparte

    @Column(name = "Ingreso")
    private Integer ingreso;

    @Column(name = "Salida")
    private Integer salida;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true; // true = activo, false = desactivado
}
