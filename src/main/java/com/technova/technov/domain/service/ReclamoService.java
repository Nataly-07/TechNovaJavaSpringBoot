package com.technova.technov.domain.service;

import java.util.List;
import com.technova.technov.domain.dto.ReclamoDto;

public interface ReclamoService {
    ReclamoDto crearReclamo(Integer usuarioId, String titulo, String descripcion, String prioridad);
    ReclamoDto responder(Integer id, String respuesta);
    ReclamoDto cerrar(Integer id);
    List<ReclamoDto> listarPorUsuario(Integer usuarioId);
    List<ReclamoDto> listarPorEstado(String estado);
    List<ReclamoDto> listarTodos();
    ReclamoDto detalle(Integer id);
    ReclamoDto actualizar(Integer id, ReclamoDto dto);
    boolean eliminar(Integer id);
    ReclamoDto enviarAlAdministrador(Integer id);
    List<ReclamoDto> listarQuejasEnviadasPorEmpleados();
    ReclamoDto evaluarResolucion(Integer id, String evaluacion);
}

