package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.FavoritoDto;
import java.util.List;

public interface FavoritoService {
    List<FavoritoDto> listarTodos();
    List<FavoritoDto> listarPorUsuario(Long usuarioId);
    FavoritoDto agregar(Long usuarioId, Integer productoId);
    FavoritoDto eliminar(Long usuarioId, Integer productoId);
    boolean toggle(Long usuarioId, Integer productoId);
}
