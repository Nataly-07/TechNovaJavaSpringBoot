package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.NotificacionDto;
import com.technova.technov.domain.entity.Usuario;
import java.time.Instant;
import java.util.List;

public interface NotificacionService {
    List<NotificacionDto> listarTodos();

    List<NotificacionDto> listarPorUsuario(Long userId, boolean soloNoLeidas);

    List<NotificacionDto> listarPorUsuarioYLeida(Long userId, boolean leida);

    List<NotificacionDto> listarPorUsuarioYRango(Long userId, Instant desde, Instant hasta);

    NotificacionDto crear(NotificacionDto dto);

    // Sobrecarga para facilitar creación desde servicios
    NotificacionDto crear(Usuario usuario, String titulo, String mensaje, String tipo, String icono);

    NotificacionDto marcarLeida(Long id);

    void crearNotificacionSistema(String titulo, String mensaje, String modulo, String icono);

    void crearNotificacionRol(String role, String titulo, String mensaje, String modulo, String icono);
}
