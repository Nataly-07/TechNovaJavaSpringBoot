package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.PagoDto;
import java.util.List;

public interface PagoService {
    PagoDto registrar(PagoDto pago);
    List<PagoDto> listarTodos();
}
