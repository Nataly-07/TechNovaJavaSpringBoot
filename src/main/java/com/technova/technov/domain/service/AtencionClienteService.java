package com.technova.technov.domain.service;

import java.util.List;
import com.technova.technov.domain.dto.AtencionClienteDto;

public interface AtencionClienteService {
    AtencionClienteDto crearTicket(Integer usuarioId, String tema, String descripcion);
    AtencionClienteDto responder(Integer id, String respuesta);
    AtencionClienteDto cerrar(Integer id);
    List<AtencionClienteDto> listarPorUsuario(Integer usuarioId);
    List<AtencionClienteDto> listarPorEstado(String estado);
    AtencionClienteDto detalle(Integer id);
    AtencionClienteDto actualizar(Integer id, AtencionClienteDto dto);
    boolean eliminar(Integer id);
}
