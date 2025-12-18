package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.OrdenCompraDto;
import java.util.List;

public interface OrdenCompraService {
    List<OrdenCompraDto> listarOrdenes();

    OrdenCompraDto crearOrden(OrdenCompraDto ordenDto);

    OrdenCompraDto recibirOrden(Integer id);

    OrdenCompraDto obtenerOrdenPorId(Integer id);
}
