package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.NotificacionDto;
import java.time.Instant;
import java.util.List;

public interface NotificacionService {
    List<NotificacionDto> listarTodos();
    List<NotificacionDto> listarPorUsuario(Long userId);
    List<NotificacionDto> listarPorUsuarioYLeida(Long userId, boolean leida);
    List<NotificacionDto> listarPorUsuarioYRango(Long userId, Instant desde, Instant hasta);
    NotificacionDto crear(NotificacionDto dto);
    NotificacionDto marcarLeida(Long id);
}
