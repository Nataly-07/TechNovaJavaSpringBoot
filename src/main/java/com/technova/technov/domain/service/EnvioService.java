package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.EnvioDto;
import java.util.List;

public interface EnvioService {
    List<EnvioDto> listarTodos();
    List<EnvioDto> listarPorVenta(Integer ventaId);
    EnvioDto detalle(Integer id);
    EnvioDto crear(EnvioDto dto);
    EnvioDto actualizar(Integer id, EnvioDto dto);
    boolean eliminar(Integer id);
}
