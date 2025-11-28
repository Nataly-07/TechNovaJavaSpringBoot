package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.MedioDePagoDto;
import java.util.List;

public interface MedioDePagoService {
    List<MedioDePagoDto> listar();
    MedioDePagoDto obtener(Integer id);
    MedioDePagoDto guardar(MedioDePagoDto m);
    boolean eliminar(Integer id);
}
