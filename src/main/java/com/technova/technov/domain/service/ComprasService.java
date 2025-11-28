package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.CompraRequestDto;
import java.util.List;
public interface ComprasService {
    CompraDto crear(CompraRequestDto request);
    List<CompraDto> listar();
    CompraDto detalle(Integer id);
    CompraDto actualizar(Integer id, CompraRequestDto request);
    boolean eliminar(Integer id);
}
