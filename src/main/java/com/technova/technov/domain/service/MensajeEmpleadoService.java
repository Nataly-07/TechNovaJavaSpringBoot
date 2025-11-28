package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.MensajeEmpleadoDto;
import java.util.List;

public interface MensajeEmpleadoService {
    List<MensajeEmpleadoDto> listarTodos();
    List<MensajeEmpleadoDto> listarPorEmpleado(Long empleadoId);
    List<MensajeEmpleadoDto> listarPorTipoYPrioridad(String tipo, String prioridad);
    MensajeEmpleadoDto crear(MensajeEmpleadoDto dto);
    MensajeEmpleadoDto marcarLeido(Long id);
}
