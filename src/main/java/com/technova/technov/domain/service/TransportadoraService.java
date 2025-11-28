package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.TransportadoraDto;
import java.util.List;

public interface TransportadoraService {
    List<TransportadoraDto> listarTodos();
    List<TransportadoraDto> listarPorEnvio(Integer envioId);
    TransportadoraDto detalle(Integer id);
    TransportadoraDto crear(TransportadoraDto dto);
    TransportadoraDto actualizar(Integer id, TransportadoraDto dto);
    boolean eliminar(Integer id);
}
