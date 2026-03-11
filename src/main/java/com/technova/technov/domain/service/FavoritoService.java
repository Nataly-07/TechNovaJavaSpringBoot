package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.FavoritoDto;
import java.util.List;

public interface FavoritoService {
    List<FavoritoDto> listarTodos();
    List<FavoritoDto> listarPorUsuario(Integer usuarioId);
    FavoritoDto agregar(Integer usuarioId, Integer productoId);
    FavoritoDto eliminar(Integer usuarioId, Integer productoId);
    boolean toggle(Integer usuarioId, Integer productoId);
}
