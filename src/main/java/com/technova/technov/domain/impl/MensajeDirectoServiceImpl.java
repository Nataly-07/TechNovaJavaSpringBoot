package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.MensajeDirectoDto;
import com.technova.technov.domain.dto.NotificacionDto;
import com.technova.technov.domain.entity.MensajeDirecto;
import com.technova.technov.domain.repository.MensajeDirectoRepository;
import com.technova.technov.domain.service.MensajeDirectoService;
import com.technova.technov.domain.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeDirectoServiceImpl implements MensajeDirectoService {

    private final MensajeDirectoRepository mensajeDirectoRepository;

    @Autowired
    private NotificacionService notificacionService;

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
                        if (fechaCompare != 0)
                            return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarPorUsuario(Integer userId) {
        return mensajeDirectoRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDirectoDto> listarPorEmpleado(Integer empleadoId) {
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
    public MensajeDirectoDto crearConversacion(Integer userId, String asunto, String mensaje, String prioridad) {
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
        MensajeDirecto saved = mensajeDirectoRepository.save(entity);

        // Notificación de sistema
        notificacionService.crearNotificacionSistema(
                "Nuevo Mensaje Recibido",
                "El usuario " + userId + " ha iniciado una nueva conversación: " + asunto,
                "Mensajes",
                "bx-message-detail");

        // Notificación al Empleado (Mensajes)
        notificacionService.crearNotificacionRol(
                "EMPLEADO",
                "Nuevo Mensaje Recibido",
                "Has recibido un nuevo mensaje de un usuario sobre: " + asunto,
                "Mensajes",
                "bx-message-detail");

        return toDto(saved);
    }

    @Override
    @Transactional
    public MensajeDirectoDto responderMensaje(Integer parentMessageId, Integer senderId, String senderType, String mensaje) {
        MensajeDirecto parentMessage = mensajeDirectoRepository.findById(parentMessageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje padre no encontrado: " + parentMessageId));

        // Si un empleado responde, marcar el mensaje original como leído y respondido
        if ("empleado".equalsIgnoreCase(senderType)) {
            parentMessage.setRead(true);
            parentMessage.setReadAt(Instant.now());
            parentMessage.setEstado("respondido");
            parentMessage.setUpdatedAt(Instant.now());
            mensajeDirectoRepository.save(parentMessage);

            // Crear notificación para el cliente cuando un empleado responde
            Integer usuarioId = parentMessage.getUserId();
            if (usuarioId != null) {
                try {
                    System.out.println("=== CREAR NOTIFICACIÓN DE RESPUESTA A MENSAJE ===");
                    System.out.println("  -> Mensaje ID: " + parentMessageId);
                    System.out.println("  -> Usuario ID: " + usuarioId);

                    String asunto = parentMessage.getAsunto();
                    String mensajeNotificacion = mensaje != null && mensaje.length() > 100
                            ? mensaje.substring(0, 100) + "..."
                            : mensaje;

                    // Crear JSON con datos adicionales
                    ObjectMapper objectMapper = new ObjectMapper();
                    java.util.Map<String, Object> dataAdicional = new java.util.HashMap<>();
                    dataAdicional.put("mensajeId", parentMessageId);
                    dataAdicional.put("asunto", asunto);
                    String dataAdicionalJson = objectMapper.writeValueAsString(dataAdicional);

                    NotificacionDto notificacion = NotificacionDto.builder()
                            .userId(usuarioId)
                            .titulo("Respuesta a tu mensaje")
                            .mensaje(mensajeNotificacion)
                            .tipo("Mensajes")
                            .icono("bx-message")
                            .leida(false)
                            .dataAdicional(dataAdicionalJson)
                            .build();

                    NotificacionDto notificacionCreada = notificacionService.crear(notificacion);
                    System.out.println("=== NOTIFICACIÓN: Notificación de respuesta a mensaje creada exitosamente ===");
                    System.out.println("  -> Notificación ID: "
                            + (notificacionCreada != null ? notificacionCreada.getId() : "null"));
                } catch (Exception e) {
                    System.err.println("=== ERROR: No se pudo crear la notificación de respuesta a mensaje ===");
                    System.err.println("  -> Error: " + e.getMessage());
                    System.err.println("  -> Stack trace:");
                    e.printStackTrace();
                    // No lanzar excepción para no interrumpir el proceso
                }
            } else {
                System.err.println("=== ADVERTENCIA: No se pudo crear notificación - Usuario ID es null ===");
                System.err.println("  -> Mensaje ID: " + parentMessageId);
            }

        // Si un ADMINISTRADOR responde a un EMPLEADO, notificar al empleado
        if ("admin".equalsIgnoreCase(senderType) && "empleado".equalsIgnoreCase(parentMessage.getSenderType())) {
            String descCorto = mensaje != null && mensaje.length() > 100
                    ? mensaje.substring(0, 100) + "..."
                    : mensaje;
            
            // Notificamos al empleado específico que envió el mensaje original
            Integer empleadoId = parentMessage.getSenderId();
            if (empleadoId != null) {
                NotificacionDto notif = NotificacionDto.builder()
                        .userId(empleadoId)
                        .titulo("Respuesta a tu mensaje")
                        .mensaje(descCorto)
                        .tipo("Mensajes")
                        .icono("bx-message-check")
                        .leida(false)
                        .build();
                notificacionService.crear(notif);
            }
        }
        }

        Integer recipientId = "empleado".equalsIgnoreCase(senderType) ? parentMessage.getUserId() : null;

        MensajeDirecto reply = MensajeDirecto.builder()
                .conversationId(parentMessage.getConversationId())
                .parentMessageId(parentMessageId)
                .senderType(senderType)
                .senderId(senderId)
                .recipientId(recipientId)
                .isRead(false)
                .readAt(null)
                .userId("empleado".equalsIgnoreCase(senderType) ? senderId : parentMessage.getUserId())
                .asunto(parentMessage.getAsunto() != null && parentMessage.getAsunto().toLowerCase().startsWith("re:")
                        ? parentMessage.getAsunto()
                        : "Re: " + (parentMessage.getAsunto() != null ? parentMessage.getAsunto() : ""))
                .mensaje(mensaje)
                .prioridad(parentMessage.getPrioridad())
                .estado("enviado")
                .empleadoId("empleado".equalsIgnoreCase(senderType) ? senderId : null)
                .respuesta(null)
                .fechaRespuesta(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        MensajeDirecto savedReply = mensajeDirectoRepository.save(reply);

        // Si responde un cliente (no empleado), notificar al sistema
        if (!"empleado".equalsIgnoreCase(senderType)) {
            notificacionService.crearNotificacionSistema(
                    "Respuesta Recibida",
                    "Nueva respuesta en conversación sobre: " + parentMessage.getAsunto(),
                    "Mensajes",
                    "bx-message-dots");
        }

        return toDto(savedReply);
    }

    @Override
    @Transactional
    public MensajeDirectoDto marcarLeido(Integer id) {
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
    public MensajeDirectoDto obtenerPorId(Integer id) {
        return mensajeDirectoRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    private MensajeDirectoDto toDto(MensajeDirecto m) {
        if (m == null)
            return null;
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
