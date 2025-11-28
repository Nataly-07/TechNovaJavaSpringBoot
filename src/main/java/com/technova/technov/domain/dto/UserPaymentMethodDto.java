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
public class UserPaymentMethodDto {
    private Long id;
    private Long userId;
    private String metodoPago;
    private boolean isDefault;
    private String brand;
    private String last4;
    private String holderName;
    private String token;
    private String expMonth;
    private String expYear;
    private String email;
    private String phone;
    private Integer installments;
    private Instant createdAt;
    private Instant updatedAt;
}
