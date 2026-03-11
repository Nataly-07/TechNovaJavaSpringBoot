package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import java.util.List;

public interface MensajeDirectoService {
    List<MensajeDirectoDto> listarTodos();
    List<MensajeDirectoDto> listarPorUsuario(Integer userId);
    List<MensajeDirectoDto> listarPorEmpleado(Integer empleadoId);
    List<MensajeDirectoDto> listarPorConversacion(String conversationId);
    MensajeDirectoDto crear(MensajeDirectoDto dto);
    MensajeDirectoDto crearConversacion(Integer userId, String asunto, String mensaje, String prioridad);
    MensajeDirectoDto responderMensaje(Integer parentMessageId, Integer senderId, String senderType, String mensaje);
    MensajeDirectoDto marcarLeido(Integer id);
    MensajeDirectoDto obtenerPorId(Integer id);
}
