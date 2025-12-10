package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.CaracteristicasDto;

import java.util.List;
import java.util.Optional;

public interface CaracteristicaService {
    List<CaracteristicasDto> listar();
    CaracteristicasDto crear(CaracteristicasDto dto);
    Optional<CaracteristicasDto> caracteristicaPorId(Integer id);
    CaracteristicasDto actualizar(Integer id, CaracteristicasDto dto);
    boolean eliminar(Integer id);
    List<String> listarCategorias();
    List<String> listarMarcas();
}
