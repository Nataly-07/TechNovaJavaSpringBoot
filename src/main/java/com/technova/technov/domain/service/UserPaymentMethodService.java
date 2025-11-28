package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.UserPaymentMethodDto;
import java.util.List;

public interface UserPaymentMethodService {
    List<UserPaymentMethodDto> listarTodos();
    List<UserPaymentMethodDto> listarPorUsuario(Integer usuarioId);
    UserPaymentMethodDto guardar(Integer usuarioId, UserPaymentMethodDto upm);
    void eliminar(Long id);
}
