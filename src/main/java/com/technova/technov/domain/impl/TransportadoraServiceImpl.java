package com.technova.technov.domain.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.TransportadoraDto;
import com.technova.technov.domain.entity.Envio;
import com.technova.technov.domain.entity.Transportadora;
import com.technova.technov.domain.repository.EnvioRepository;
import com.technova.technov.domain.repository.TransportadoraRepository;
import com.technova.technov.domain.service.TransportadoraService;

@Service
public class TransportadoraServiceImpl implements TransportadoraService {

    @Autowired
    private TransportadoraRepository transportadoraRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TransportadoraDto> listarTodos() {
        return transportadoraRepository.findByEstadoTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportadoraDto> listarPorEnvio(Integer envioId) {
        List<Transportadora> transportadoras = transportadoraRepository.findByEnvio_IdAndEstadoTrue(envioId);
        return transportadoras.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransportadoraDto detalle(Integer id) {
        return transportadoraRepository.findByIdAndEstadoTrue(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public TransportadoraDto crear(TransportadoraDto dto) {
        Envio envio = envioRepository.findByIdAndEstadoTrue(dto.getEnvioId())
                .orElseThrow(() -> new IllegalArgumentException("Envio no encontrado: " + dto.getEnvioId()));
        Transportadora entity = modelMapper.map(dto, Transportadora.class);
        entity.setId(null);
        entity.setEnvio(envio);
        entity.setEstado(true); 
        Transportadora saved = transportadoraRepository.save(entity);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public TransportadoraDto actualizar(Integer id, TransportadoraDto dto) {
        return transportadoraRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    Envio envio = envioRepository.findByIdAndEstadoTrue(dto.getEnvioId())
                            .orElseThrow(() -> new IllegalArgumentException("Envio no encontrado: " + dto.getEnvioId()));
                    existing.setEnvio(envio);
                    Transportadora actualizada = transportadoraRepository.save(existing);
                    return convertToDto(actualizada);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return transportadoraRepository.findById(id)
                .map(transportadora -> {
                    transportadora.setEstado(false); 
                    transportadoraRepository.save(transportadora);
                    return true;
                })
                .orElse(false);
    }

    private TransportadoraDto convertToDto(Transportadora transportadora) {
        TransportadoraDto dto = modelMapper.map(transportadora, TransportadoraDto.class);
        if (transportadora.getEnvio() != null) {
            dto.setEnvioId(transportadora.getEnvio().getId());
        }
        return dto;
    }
}
