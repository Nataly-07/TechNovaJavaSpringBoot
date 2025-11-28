package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.CarritoItemDto;
import java.util.List;

public interface CarritoService {
    List<CarritoItemDto> listar(Integer usuarioId);
    List<CarritoItemDto> agregar(Integer usuarioId, Integer productoId, Integer cantidad);
    List<CarritoItemDto> actualizar(Integer usuarioId, Integer detalleId, Integer cantidad);
    List<CarritoItemDto> eliminar(Integer usuarioId, Integer detalleId);
    void vaciar(Integer usuarioId);
}
