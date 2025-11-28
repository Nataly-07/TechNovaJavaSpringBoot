package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.ResumenVentasDto;
import com.technova.technov.domain.dto.VentaDto;
import com.technova.technov.domain.dto.VentaRequestDto;
import java.time.LocalDate;
import java.util.List;

public interface VentaService {
    List<VentaDto> listar();
    VentaDto detalle(Integer id);
    List<VentaDto> porUsuario(Integer usuarioId);
    ResumenVentasDto resumen(LocalDate desde, LocalDate hasta);
    VentaDto crear(VentaRequestDto request);
    VentaDto actualizar(Integer id, VentaRequestDto request);
    boolean eliminar(Integer id);
}
