package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import java.util.List;

public interface MensajeDirectoService {
    List<MensajeDirectoDto> listarTodos();
    List<MensajeDirectoDto> listarPorUsuario(Long userId);
    List<MensajeDirectoDto> listarPorEmpleado(Long empleadoId);
    List<MensajeDirectoDto> listarPorConversacion(String conversationId);
    MensajeDirectoDto crear(MensajeDirectoDto dto);
    MensajeDirectoDto marcarLeido(Long id);
}
