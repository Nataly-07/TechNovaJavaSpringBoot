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
                .sorted((a, b) -> {
                    // Ordenar por fecha de creación descendente (más reciente primero)
                    if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        int fechaCompare = b.getCreatedAt().compareTo(a.getCreatedAt());
                        if (fechaCompare != 0) return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
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
    public MensajeDirectoDto crearConversacion(Long userId, String asunto, String mensaje, String prioridad) {
        String conversationId = "conv_" + System.currentTimeMillis() + "_" + userId;
        
        MensajeDirecto entity = MensajeDirecto.builder()
                .conversationId(conversationId)
                .parentMessageId(null)
                .senderType("cliente")
                .senderId(userId)
                .recipientId(null)
                .isRead(false)
                .readAt(null)
                .userId(userId)
                .asunto(asunto)
                .mensaje(mensaje)
                .prioridad(prioridad != null ? prioridad : "normal")
                .estado("enviado")
                .empleadoId(null)
                .respuesta(null)
                .fechaRespuesta(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toDto(mensajeDirectoRepository.save(entity));
    }

    @Override
    @Transactional
    public MensajeDirectoDto responderMensaje(Long parentMessageId, Long senderId, String senderType, String mensaje) {
        MensajeDirecto parentMessage = mensajeDirectoRepository.findById(parentMessageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje padre no encontrado: " + parentMessageId));
        
        // Si un empleado responde, marcar el mensaje original como leído y respondido
        if ("empleado".equalsIgnoreCase(senderType)) {
            parentMessage.setRead(true);
            parentMessage.setReadAt(Instant.now());
            parentMessage.setEstado("respondido");
            parentMessage.setUpdatedAt(Instant.now());
            mensajeDirectoRepository.save(parentMessage);
        }
        
        Long recipientId = "empleado".equalsIgnoreCase(senderType) ? parentMessage.getUserId() : null;
        
        MensajeDirecto reply = MensajeDirecto.builder()
                .conversationId(parentMessage.getConversationId())
                .parentMessageId(parentMessageId)
                .senderType(senderType)
                .senderId(senderId)
                .recipientId(recipientId)
                .isRead(false)
                .readAt(null)
                .userId("empleado".equalsIgnoreCase(senderType) ? senderId : parentMessage.getUserId())
                .asunto("Re: " + parentMessage.getAsunto())
                .mensaje(mensaje)
                .prioridad(parentMessage.getPrioridad())
                .estado("enviado")
                .empleadoId("empleado".equalsIgnoreCase(senderType) ? senderId : null)
                .respuesta(null)
                .fechaRespuesta(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toDto(mensajeDirectoRepository.save(reply));
    }

    @Override
    @Transactional
    public MensajeDirectoDto marcarLeido(Long id) {
        return mensajeDirectoRepository.findById(id)
                .map(m -> {
                    m.setRead(true);
                    m.setReadAt(Instant.now());
                    m.setUpdatedAt(Instant.now());
                    if ("enviado".equalsIgnoreCase(m.getEstado())) {
                        m.setEstado("leido");
                    }
                    return toDto(mensajeDirectoRepository.save(m));
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public MensajeDirectoDto obtenerPorId(Long id) {
        return mensajeDirectoRepository.findById(id)
                .map(this::toDto)
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
