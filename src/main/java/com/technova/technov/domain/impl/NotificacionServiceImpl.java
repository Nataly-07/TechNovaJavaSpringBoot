package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.NotificacionDto;
import com.technova.technov.domain.entity.Notificacion;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.NotificacionRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionServiceImpl(NotificacionRepository notificacionRepository,
                                   UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarTodos() {
        return notificacionRepository.findAll().stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de creación descendente (más reciente primero)
                    if (a.getFechaCreacion() != null && b.getFechaCreacion() != null) {
                        int fechaCompare = b.getFechaCreacion().compareTo(a.getFechaCreacion());
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
    public List<NotificacionDto> listarPorUsuario(Long userId) {
        return notificacionRepository.findByUsuario_IdOrderByFechaCreacionDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorUsuarioYLeida(Long userId, boolean leida) {
        return notificacionRepository.findByUsuario_IdAndLeidaOrderByFechaCreacionDesc(userId, leida)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorUsuarioYRango(Long userId, Instant desde, Instant hasta) {
        return notificacionRepository.findByUsuario_IdAndFechaCreacionBetween(userId, desde, hasta)
                .stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de creación descendente (más reciente primero)
                    if (a.getFechaCreacion() != null && b.getFechaCreacion() != null) {
                        int fechaCompare = b.getFechaCreacion().compareTo(a.getFechaCreacion());
                        if (fechaCompare != 0) return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificacionDto crear(NotificacionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("NotificacionDto no puede ser null");
        }
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("UserId no puede ser null");
        }
        
        System.out.println("=== CREAR NOTIFICACIÓN ===");
        System.out.println("  -> UserId: " + dto.getUserId());
        System.out.println("  -> Título: " + dto.getTitulo());
        System.out.println("  -> Tipo: " + dto.getTipo());
        
        Usuario usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getUserId()));
        
        System.out.println("  -> Usuario encontrado: " + usuario.getEmail());
        
        Notificacion entity = Notificacion.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo() != null ? dto.getTitulo() : "Notificación")
                .mensaje(dto.getMensaje() != null ? dto.getMensaje() : "")
                .tipo(dto.getTipo() != null ? dto.getTipo() : "general")
                .icono(dto.getIcono() != null ? dto.getIcono() : "bx-bell")
                .leida(false)
                .dataAdicional(dto.getDataAdicional())
                .fechaCreacion(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Notificacion saved = notificacionRepository.save(entity);
        System.out.println("  -> Notificación guardada con ID: " + saved.getId());
        
        return toDto(saved);
    }

    @Override
    @Transactional
    public NotificacionDto marcarLeida(Long id) {
        return notificacionRepository.findById(id)
                .map(n -> {
                    n.setLeida(true);
                    n.setUpdatedAt(Instant.now());
                    return toDto(notificacionRepository.save(n));
                })
                .orElse(null);
    }

    private NotificacionDto toDto(Notificacion n) {
        if (n == null) return null;
        return NotificacionDto.builder()
                .id(n.getId())
                .userId(n.getUsuario() != null ? n.getUsuario().getId() : null)
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .icono(n.getIcono())
                .leida(n.isLeida())
                .dataAdicional(n.getDataAdicional())
                .fechaCreacion(n.getFechaCreacion())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
