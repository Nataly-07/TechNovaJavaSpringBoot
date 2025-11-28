package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import com.technova.technov.domain.entity.MensajeDirecto;
import com.technova.technov.domain.repository.MensajeDirectoRepository;
import com.technova.technov.domain.service.MensajeDirectoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeDirectoServiceImpl implements MensajeDirectoService {

    private final MensajeDirectoRepository mensajeDirectoRepository;

    public MensajeDirectoServiceImpl(MensajeDirectoRepository mensajeDirectoRepository) {
        this.mensajeDirectoRepository = mensajeDirectoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarTodos() {
        return mensajeDirectoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarPorUsuario(Long userId) {
        return mensajeDirectoRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarPorEmpleado(Long empleadoId) {
        return mensajeDirectoRepository.findByEmpleadoIdOrderByCreatedAtDesc(empleadoId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarPorConversacion(String conversationId) {
        return mensajeDirectoRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MensajeDirectoDto crear(MensajeDirectoDto dto) {
        MensajeDirecto entity = MensajeDirecto.builder()
                .conversationId(dto.getConversationId())
                .parentMessageId(dto.getParentMessageId())
                .senderType(dto.getSenderType())
                .senderId(dto.getSenderId())
                .recipientId(dto.getRecipientId())
                .isRead(false)
                .readAt(null)
                .userId(dto.getUserId())
                .asunto(dto.getAsunto())
                .mensaje(dto.getMensaje())
                .prioridad(dto.getPrioridad())
                .estado(dto.getEstado())
                .empleadoId(dto.getEmpleadoId())
                .respuesta(dto.getRespuesta())
                .fechaRespuesta(dto.getFechaRespuesta())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toDto(mensajeDirectoRepository.save(entity));
    }

    @Override
    @Transactional
    public MensajeDirectoDto marcarLeido(Long id) {
        return mensajeDirectoRepository.findById(id)
                .map(m -> {
                    m.setRead(true);
                    m.setReadAt(Instant.now());
                    m.setUpdatedAt(Instant.now());
                    return toDto(mensajeDirectoRepository.save(m));
                })
                .orElse(null);
    }

    private MensajeDirectoDto toDto(MensajeDirecto m) {
        if (m == null) return null;
        return MensajeDirectoDto.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .parentMessageId(m.getParentMessageId())
                .senderType(m.getSenderType())
                .senderId(m.getSenderId())
                .recipientId(m.getRecipientId())
                .isRead(m.isRead())
                .readAt(m.getReadAt())
                .userId(m.getUserId())
                .asunto(m.getAsunto())
                .mensaje(m.getMensaje())
                .prioridad(m.getPrioridad())
                .estado(m.getEstado())
                .empleadoId(m.getEmpleadoId())
                .respuesta(m.getRespuesta())
                .fechaRespuesta(m.getFechaRespuesta())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
