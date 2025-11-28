package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.EnvioDto;
import com.technova.technov.domain.entity.Envio;
import com.technova.technov.domain.entity.Venta;
import com.technova.technov.domain.repository.EnvioRepository;
import com.technova.technov.domain.repository.VentaRepository;
import com.technova.technov.domain.service.EnvioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvioServiceImpl implements EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDto> listarTodos() {
        return envioRepository.findByEstadoTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDto> listarPorVenta(Integer ventaId) {
        List<Envio> envios = envioRepository.findByVenta_IdAndEstadoTrue(ventaId);
        return envios.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioDto detalle(Integer id) {
        return envioRepository.findByIdAndEstadoTrue(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public EnvioDto crear(EnvioDto dto) {
        Venta venta = ventaRepository.findByIdAndEstadoTrue(dto.getVentaId())
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + dto.getVentaId()));
        Envio entity = modelMapper.map(dto, Envio.class);
        entity.setId(null);
        entity.setVenta(venta);
        entity.setEstado(true); // true = activo
        Envio saved = envioRepository.save(entity);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public EnvioDto actualizar(Integer id, EnvioDto dto) {
        return envioRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    Venta venta = ventaRepository.findByIdAndEstadoTrue(dto.getVentaId())
                            .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + dto.getVentaId()));
                    existing.setVenta(venta);
                    Envio actualizado = envioRepository.save(existing);
                    return convertToDto(actualizado);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return envioRepository.findById(id)
                .map(envio -> {
                    envio.setEstado(false); // false = desactivado
                    envioRepository.save(envio);
                    return true;
                })
                .orElse(false);
    }

    private EnvioDto convertToDto(Envio envio) {
        EnvioDto dto = modelMapper.map(envio, EnvioDto.class);
        if (envio.getVenta() != null) {
            dto.setVentaId(envio.getVenta().getId());
        }
        return dto;
    }
}
