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
        if (dto == null) {
            System.err.println("ERROR: PagoDto es null");
            return null;
        }
        
        System.out.println("=== PagoServiceImpl.registrar ===");
        System.out.println("  -> DTO recibido - Factura: " + dto.getNumeroFactura() + ", Monto: " + dto.getMonto());
        
        Pago pago = modelMapper.map(dto, Pago.class);
        pago.setId(null);
        
        // Validar y establecer valores por defecto
        if (pago.getEstadoPago() == null) {
            pago.setEstadoPago("CONFIRMADO");
        }
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDate.now());
        }
        if (pago.getFechaFactura() == null) {
            pago.setFechaFactura(LocalDate.now());
        }
        
        // Validar que el número de factura no sea null
        if (pago.getNumeroFactura() == null || pago.getNumeroFactura().trim().isEmpty()) {
            System.err.println("ERROR: Número de factura es null o vacío");
            throw new IllegalArgumentException("El número de factura no puede ser null o vacío");
        }
        
        System.out.println("  -> Pago entity antes de guardar - Factura: " + pago.getNumeroFactura() + ", Monto: " + pago.getMonto());
        
        Pago saved = pagoRepository.save(pago);
        
        System.out.println("  -> Pago guardado - ID: " + saved.getId() + ", Factura: " + saved.getNumeroFactura());
        
        PagoDto result = modelMapper.map(saved, PagoDto.class);
        System.out.println("  -> DTO resultado - ID: " + result.getId() + ", Factura: " + result.getNumeroFactura());
        
        return result;
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
