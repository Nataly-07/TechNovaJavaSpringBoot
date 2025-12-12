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
        try {
            return mensajeEmpleadoRepository.findAll().stream()
                    .filter(m -> m != null)
                    .sorted((a, b) -> {
                        try {
                            // Ordenar por fecha de creación descendente (más reciente primero)
                            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                                int fechaCompare = b.getCreatedAt().compareTo(a.getCreatedAt());
                                if (fechaCompare != 0) return fechaCompare;
                            }
                            // Si las fechas son iguales o nulas, ordenar por ID descendente
                            if (a.getId() != null && b.getId() != null) {
                                return b.getId().compareTo(a.getId());
                            }
                            return 0;
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .map(this::toDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al listar todos los mensajes: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
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
        try {
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
        } catch (Exception e) {
            System.err.println("Error al convertir MensajeEmpleado a DTO: " + e.getMessage());
            e.printStackTrace();
            // Retornar un DTO básico en caso de error
            MensajeEmpleadoDto dto = new MensajeEmpleadoDto();
            dto.setId(m.getId());
            dto.setEmpleadoId(m.getEmpleadoId());
            dto.setRemitenteId(m.getRemitenteId());
            dto.setTipoRemitente(m.getTipoRemitente());
            dto.setAsunto(m.getAsunto() != null ? m.getAsunto() : "");
            dto.setMensaje(m.getMensaje() != null ? m.getMensaje() : "");
            dto.setTipo(m.getTipo() != null ? m.getTipo() : "general");
            dto.setPrioridad(m.getPrioridad() != null ? m.getPrioridad() : "normal");
            dto.setLeido(m.isLeido());
            dto.setFechaLeido(m.getFechaLeido());
            dto.setDataAdicional(m.getDataAdicional());
            dto.setCreatedAt(m.getCreatedAt());
            dto.setUpdatedAt(m.getUpdatedAt());
            return dto;
        }
    }
}
