package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.PagoDto;
import com.technova.technov.domain.entity.Pago;
import com.technova.technov.domain.repository.PagoRepository;
import com.technova.technov.domain.service.PagoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public PagoDto registrar(PagoDto dto) {
        if (dto == null) return null;
        Pago pago = modelMapper.map(dto, Pago.class);
        pago.setId(null);
        if (pago.getEstadoPago() == null) {
            pago.setEstadoPago("CONFIRMADO");
        }
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDate.now());
        }
        if (pago.getFechaFactura() == null) {
            pago.setFechaFactura(LocalDate.now());
        }
        Pago saved = pagoRepository.save(pago);
        return modelMapper.map(saved, PagoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDto> listarTodos() {
        List<Pago> pagos = pagoRepository.findAll();
        return pagos.stream()
                .map(pago -> modelMapper.map(pago, PagoDto.class))
                .collect(Collectors.toList());
    }
}
