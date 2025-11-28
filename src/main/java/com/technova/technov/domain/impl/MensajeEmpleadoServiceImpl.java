package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.MensajeEmpleadoDto;
import com.technova.technov.domain.entity.MensajeEmpleado;
import com.technova.technov.domain.repository.MensajeEmpleadoRepository;
import com.technova.technov.domain.service.MensajeEmpleadoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeEmpleadoServiceImpl implements MensajeEmpleadoService {

    private final MensajeEmpleadoRepository mensajeEmpleadoRepository;

    public MensajeEmpleadoServiceImpl(MensajeEmpleadoRepository mensajeEmpleadoRepository) {
        this.mensajeEmpleadoRepository = mensajeEmpleadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeEmpleadoDto> listarTodos() {
        return mensajeEmpleadoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeEmpleadoDto> listarPorEmpleado(Long empleadoId) {
        return mensajeEmpleadoRepository.findByEmpleadoIdOrderByCreatedAtDesc(empleadoId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeEmpleadoDto> listarPorTipoYPrioridad(String tipo, String prioridad) {
        return mensajeEmpleadoRepository.findByTipoAndPrioridadOrderByCreatedAtDesc(tipo, prioridad)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MensajeEmpleadoDto crear(MensajeEmpleadoDto dto) {
        MensajeEmpleado entity = MensajeEmpleado.builder()
                .empleadoId(dto.getEmpleadoId())
                .remitenteId(dto.getRemitenteId())
                .tipoRemitente(dto.getTipoRemitente())
                .asunto(dto.getAsunto())
                .mensaje(dto.getMensaje())
                .tipo(dto.getTipo())
                .prioridad(dto.getPrioridad())
                .leido(false)
                .fechaLeido(null)
                .dataAdicional(dto.getDataAdicional())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toDto(mensajeEmpleadoRepository.save(entity));
    }

    @Override
    @Transactional
    public MensajeEmpleadoDto marcarLeido(Long id) {
        return mensajeEmpleadoRepository.findById(id)
                .map(m -> {
                    m.setLeido(true);
                    m.setFechaLeido(Instant.now());
                    m.setUpdatedAt(Instant.now());
                    return toDto(mensajeEmpleadoRepository.save(m));
                })
                .orElse(null);
    }

    private MensajeEmpleadoDto toDto(MensajeEmpleado m) {
        if (m == null) return null;
        return MensajeEmpleadoDto.builder()
                .id(m.getId())
                .empleadoId(m.getEmpleadoId())
                .remitenteId(m.getRemitenteId())
                .tipoRemitente(m.getTipoRemitente())
                .asunto(m.getAsunto())
                .mensaje(m.getMensaje())
                .tipo(m.getTipo())
                .prioridad(m.getPrioridad())
                .leido(m.isLeido())
                .fechaLeido(m.getFechaLeido())
                .dataAdicional(m.getDataAdicional())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
