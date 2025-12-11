package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import java.util.List;

public interface MensajeDirectoService {
    List<MensajeDirectoDto> listarTodos();
    List<MensajeDirectoDto> listarPorUsuario(Long userId);
    List<MensajeDirectoDto> listarPorEmpleado(Long empleadoId);
    List<MensajeDirectoDto> listarPorConversacion(String conversationId);
    MensajeDirectoDto crear(MensajeDirectoDto dto);
    MensajeDirectoDto crearConversacion(Long userId, String asunto, String mensaje, String prioridad);
    MensajeDirectoDto responderMensaje(Long parentMessageId, Long senderId, String senderType, String mensaje);
    MensajeDirectoDto marcarLeido(Long id);
    MensajeDirectoDto obtenerPorId(Long id);
}
