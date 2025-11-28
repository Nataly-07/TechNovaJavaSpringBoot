package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.CheckoutResponseDto;
public interface CheckoutService {
    CheckoutResponseDto checkout(Integer usuarioId);
}
