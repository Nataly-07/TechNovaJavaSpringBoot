package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad que representa un método de pago asociado a un usuario.
 *
 * <p>Incluye metadata del medio (marca, últimos dígitos, titular, token) y banderas
 * como si es predeterminado, además de marcas de tiempo.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_payment_methods")
public class UserPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "brand")
    private String brand;

    @Column(name = "last4")
    private String last4;

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "token")
    private String token;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "exp_month")
    private String expMonth;

    @Column(name = "exp_year")
    private String expYear;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "installments")
    private Integer installments;
}
