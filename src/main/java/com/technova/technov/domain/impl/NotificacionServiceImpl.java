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
                        if (fechaCompare != 0)
                            return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorUsuario(Integer userId, boolean soloNoLeidas) {
        List<Notificacion> notificaciones;
        if (soloNoLeidas) {
            notificaciones = notificacionRepository.findByUsuario_IdAndLeidaOrderByFechaCreacionDesc(userId, false);
        } else {
            notificaciones = notificacionRepository.findByUsuario_IdOrderByFechaCreacionDesc(userId);
        }
        return notificaciones.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorUsuarioYLeida(Integer userId, boolean leida) {
        return notificacionRepository.findByUsuario_IdAndLeidaOrderByFechaCreacionDesc(userId, leida)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorUsuarioYRango(Integer userId, Instant desde, Instant hasta) {
        return notificacionRepository.findByUsuario_IdAndFechaCreacionBetween(userId, desde, hasta)
                .stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de creación descendente (más reciente primero)
                    if (a.getFechaCreacion() != null && b.getFechaCreacion() != null) {
                        int fechaCompare = b.getFechaCreacion().compareTo(a.getFechaCreacion());
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
    public NotificacionDto crear(Usuario usuario, String titulo, String mensaje, String tipo, String icono) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        Notificacion entity = Notificacion.builder()
                .usuario(usuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .icono(icono != null ? icono : "bx-bell")
                .leida(false)
                .fechaCreacion(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return toDto(notificacionRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificacionDto marcarLeida(Integer id) {
        return notificacionRepository.findById(id)
                .map(n -> {
                    n.setLeida(true);
                    n.setUpdatedAt(Instant.now());
                    return toDto(notificacionRepository.save(n));
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void crearNotificacionSistema(String titulo, String mensaje, String modulo, String icono) {
        // Encontrar todos los administradores
        List<Usuario> admins = usuarioRepository.findByRoleIgnoreCase("ADMIN");

        if (admins.isEmpty()) {
            System.out
                    .println("ADVERTENCIA: No se encontraron administradores para enviar la notificación del sistema.");
            return;
        }

        Instant now = Instant.now();

        List<Notificacion> notificaciones = admins.stream().map(admin -> Notificacion.builder()
                .usuario(admin)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(modulo) // Usamos el campo 'tipo' para guardar el Módulo (Inventario, Ventas, etc.)
                .icono(icono != null ? icono : "bx-bell")
                .leida(false)
                .fechaCreacion(now)
                .createdAt(now)
                .updatedAt(now)
                .build()).collect(Collectors.toList());

        notificacionRepository.saveAll(notificaciones);
    }

    @Override
    @Transactional
    public void crearNotificacionRol(String role, String titulo, String mensaje, String modulo, String icono) {
        List<Usuario> users = usuarioRepository.findByRoleIgnoreCase(role);

        if (users.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        List<Notificacion> notificaciones = users.stream().map(user -> Notificacion.builder()
                .usuario(user)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(modulo)
                .icono(icono != null ? icono : "bx-bell")
                .leida(false)
                .fechaCreacion(now)
                .createdAt(now)
                .updatedAt(now)
                .build()).collect(Collectors.toList());

        notificacionRepository.saveAll(notificaciones);
    }

    // ... existing toDto method ...

    private NotificacionDto toDto(Notificacion n) {
        if (n == null)
            return null;
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
