package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.AtencionClienteDto;
import com.technova.technov.domain.entity.AtencionCliente;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.AtencionClienteRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.AtencionClienteService;
import com.technova.technov.domain.service.NotificacionService;
import com.technova.technov.domain.dto.NotificacionDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtencionClienteServiceImpl implements AtencionClienteService {

    @Autowired
    private AtencionClienteRepository atencionClienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificacionService notificacionService;

    @Override
    @Transactional
    public AtencionClienteDto crearTicket(Integer usuarioId, String tema, String descripcion) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        if (tema == null || tema.trim().isEmpty()) {
            throw new IllegalArgumentException("El tema no puede estar vacío");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        
        Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado o inactivo: " + usuarioId));
        
        AtencionCliente ticket = new AtencionCliente();
        ticket.setUsuario(usuario);
        ticket.setFechaConsulta(LocalDateTime.now());
        ticket.setTema(tema.trim());
        ticket.setDescripcion(descripcion.trim());
        ticket.setEstado("abierto");
        
        AtencionCliente ticketGuardado = atencionClienteRepository.save(ticket);
        return convertToDto(ticketGuardado);
    }

    @Override
    @Transactional
    public AtencionClienteDto responder(Integer id, String respuesta) {
        System.out.println("=== INICIANDO responder() ===");
        System.out.println("  -> Ticket ID: " + id);
        System.out.println("  -> Respuesta: " + (respuesta != null ? respuesta.substring(0, Math.min(50, respuesta.length())) : "null"));
        
        // Usar consulta con JOIN FETCH para cargar el usuario
        AtencionCliente t = atencionClienteRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        
        System.out.println("  -> Ticket encontrado: " + (t != null ? "Sí" : "No"));
        
        // Obtener usuarioId - ahora debería estar cargado
        Long usuarioId = null;
        String tema = t.getTema();
        
        if (t.getUsuario() != null) {
            usuarioId = t.getUsuario().getId();
            System.out.println("  -> Usuario cargado: Sí");
            System.out.println("  -> Usuario ID: " + usuarioId);
            System.out.println("  -> Usuario Email: " + t.getUsuario().getEmail());
        } else {
            System.err.println("  -> ERROR: t.getUsuario() es null");
        }
        
        // Si aún no tenemos el usuarioId, intentar obtenerlo del DTO después de guardar
        if (usuarioId == null) {
            t.setRespuesta(respuesta);
            t.setEstado("en_proceso");
            AtencionCliente ticketGuardado = atencionClienteRepository.save(t);
            AtencionClienteDto ticketRespondido = convertToDto(ticketGuardado);
            
            if (ticketRespondido != null && ticketRespondido.getUsuarioId() != null) {
                usuarioId = Long.valueOf(ticketRespondido.getUsuarioId());
                System.out.println("  -> Usuario ID obtenido del DTO: " + usuarioId);
            }
        } else {
            t.setRespuesta(respuesta);
            t.setEstado("en_proceso");
            atencionClienteRepository.save(t);
        }
        
        AtencionClienteDto ticketRespondido = convertToDto(t);
        
        // Crear notificación para el cliente
        if (usuarioId != null) {
            try {
                System.out.println("=== CREAR NOTIFICACIÓN DE RESPUESTA ===");
                System.out.println("  -> Ticket ID: " + id);
                System.out.println("  -> Usuario ID: " + usuarioId);
                System.out.println("  -> Tema: " + tema);
                
                String mensaje = String.format(
                    "Hemos respondido a tu consulta sobre '%s'. " +
                    "Revisa la respuesta en tu panel de atención al cliente.",
                    tema != null && tema.length() > 50 
                        ? tema.substring(0, 50) + "..." 
                        : (tema != null ? tema : "tu consulta")
                );
                
                // Crear JSON con datos adicionales
                ObjectMapper objectMapper = new ObjectMapper();
                java.util.Map<String, Object> dataAdicional = new java.util.HashMap<>();
                dataAdicional.put("ticketId", id);
                dataAdicional.put("tema", tema);
                String dataAdicionalJson = objectMapper.writeValueAsString(dataAdicional);
                
                NotificacionDto notificacion = NotificacionDto.builder()
                        .userId(usuarioId)
                        .titulo("Respuesta a tu consulta")
                        .mensaje(mensaje)
                        .tipo("atencion_cliente")
                        .icono("bx-headphone")
                        .leida(false)
                        .dataAdicional(dataAdicionalJson)
                        .build();
                
                NotificacionDto notificacionCreada = notificacionService.crear(notificacion);
                System.out.println("=== NOTIFICACIÓN: Notificación de respuesta creada exitosamente ===");
                System.out.println("  -> Notificación ID: " + (notificacionCreada != null ? notificacionCreada.getId() : "null"));
            } catch (Exception e) {
                System.err.println("=== ERROR: No se pudo crear la notificación de respuesta ===");
                System.err.println("  -> Error: " + e.getMessage());
                System.err.println("  -> Stack trace:");
                e.printStackTrace();
                // No lanzar excepción para no interrumpir el proceso
            }
        } else {
            System.err.println("=== ADVERTENCIA: No se pudo crear notificación - Usuario ID es null ===");
            System.err.println("  -> Ticket ID: " + id);
            System.err.println("  -> Ticket: " + (t != null ? "existe" : "null"));
            System.err.println("  -> Usuario en ticket: " + (t != null && t.getUsuario() != null ? "existe" : "null"));
            System.err.println("  -> DTO UsuarioId: " + (ticketRespondido != null ? ticketRespondido.getUsuarioId() : "null"));
        }
        
        System.out.println("=== FINALIZANDO responder() ===");
        return ticketRespondido;
    }

    @Override
    @Transactional
    public AtencionClienteDto cerrar(Integer id) {
        System.out.println("=== INICIANDO cerrar() ===");
        System.out.println("  -> Ticket ID: " + id);
        
        // Usar consulta con JOIN FETCH para cargar el usuario
        AtencionCliente t = atencionClienteRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        
        System.out.println("  -> Ticket encontrado: " + (t != null ? "Sí" : "No"));
        
        // Obtener usuarioId - ahora debería estar cargado
        Long usuarioId = null;
        String tema = t.getTema();
        
        if (t.getUsuario() != null) {
            usuarioId = t.getUsuario().getId();
            System.out.println("  -> Usuario cargado: Sí");
            System.out.println("  -> Usuario ID: " + usuarioId);
            System.out.println("  -> Usuario Email: " + t.getUsuario().getEmail());
        } else {
            System.err.println("  -> ERROR: t.getUsuario() es null");
        }
        
        // Si aún no tenemos el usuarioId, intentar obtenerlo del DTO después de guardar
        if (usuarioId == null) {
            t.setEstado("resuelto");
            AtencionCliente ticketGuardado = atencionClienteRepository.save(t);
            AtencionClienteDto ticketCerrado = convertToDto(ticketGuardado);
            
            if (ticketCerrado != null && ticketCerrado.getUsuarioId() != null) {
                usuarioId = Long.valueOf(ticketCerrado.getUsuarioId());
                System.out.println("  -> Usuario ID obtenido del DTO: " + usuarioId);
            }
        } else {
            t.setEstado("resuelto");
            atencionClienteRepository.save(t);
        }
        
        AtencionClienteDto ticketCerrado = convertToDto(t);
        
        // Crear notificación para el cliente cuando se cierra el ticket
        if (usuarioId != null) {
            try {
                System.out.println("=== CREAR NOTIFICACIÓN DE TICKET CERRADO ===");
                System.out.println("  -> Ticket ID: " + id);
                System.out.println("  -> Usuario ID: " + usuarioId);
                
                String mensaje = String.format(
                    "Tu consulta sobre '%s' ha sido resuelta y cerrada. " +
                    "Si necesitas más ayuda, puedes crear una nueva consulta.",
                    tema != null && tema.length() > 50 
                        ? tema.substring(0, 50) + "..." 
                        : (tema != null ? tema : "tu consulta")
                );
                
                // Crear JSON con datos adicionales
                ObjectMapper objectMapper = new ObjectMapper();
                java.util.Map<String, Object> dataAdicional = new java.util.HashMap<>();
                dataAdicional.put("ticketId", id);
                dataAdicional.put("tema", tema);
                dataAdicional.put("estado", "resuelto");
                String dataAdicionalJson = objectMapper.writeValueAsString(dataAdicional);
                
                NotificacionDto notificacion = NotificacionDto.builder()
                        .userId(usuarioId)
                        .titulo("Consulta resuelta")
                        .mensaje(mensaje)
                        .tipo("atencion_cliente")
                        .icono("bx-headphone")
                        .leida(false)
                        .dataAdicional(dataAdicionalJson)
                        .build();
                
                NotificacionDto notificacionCreada = notificacionService.crear(notificacion);
                System.out.println("=== NOTIFICACIÓN: Notificación de ticket cerrado creada exitosamente ===");
                System.out.println("  -> Notificación ID: " + (notificacionCreada != null ? notificacionCreada.getId() : "null"));
            } catch (Exception e) {
                System.err.println("=== ERROR: No se pudo crear la notificación de ticket cerrado ===");
                System.err.println("  -> Error: " + e.getMessage());
                System.err.println("  -> Stack trace:");
                e.printStackTrace();
                // No lanzar excepción para no interrumpir el proceso
            }
        } else {
            System.err.println("=== ADVERTENCIA: No se pudo crear notificación - Usuario ID es null ===");
            System.err.println("  -> Ticket ID: " + id);
            System.err.println("  -> Ticket: " + (t != null ? "existe" : "null"));
            System.err.println("  -> Usuario en ticket: " + (t != null && t.getUsuario() != null ? "existe" : "null"));
            System.err.println("  -> DTO UsuarioId: " + (ticketCerrado != null ? ticketCerrado.getUsuarioId() : "null"));
        }
        
        System.out.println("=== FINALIZANDO cerrar() ===");
        return ticketCerrado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtencionClienteDto> listarPorUsuario(Integer usuarioId) {
        List<AtencionCliente> tickets = atencionClienteRepository.findByUsuario_IdOrderByFechaConsultaDesc(Long.valueOf(usuarioId));
        return tickets.stream()
                .sorted((t1, t2) -> {
                    if (t1.getFechaConsulta() == null && t2.getFechaConsulta() == null) return 0;
                    if (t1.getFechaConsulta() == null) return 1;
                    if (t2.getFechaConsulta() == null) return -1;
                    return t2.getFechaConsulta().compareTo(t1.getFechaConsulta());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtencionClienteDto> listarPorEstado(String estado) {
        List<AtencionCliente> tickets = atencionClienteRepository.findByEstadoIgnoreCaseOrderByFechaConsultaDesc(estado);
        return tickets.stream()
                .sorted((t1, t2) -> {
                    if (t1.getFechaConsulta() == null && t2.getFechaConsulta() == null) return 0;
                    if (t1.getFechaConsulta() == null) return 1;
                    if (t2.getFechaConsulta() == null) return -1;
                    return t2.getFechaConsulta().compareTo(t1.getFechaConsulta());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtencionClienteDto> listarTodos() {
        // Usar findAllByOrderByFechaConsultaDesc para mantener consistencia con el conteo
        List<AtencionCliente> tickets = atencionClienteRepository.findAllByOrderByFechaConsultaDesc();
        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AtencionClienteDto detalle(Integer id) {
        return atencionClienteRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public AtencionClienteDto actualizar(Integer id, AtencionClienteDto dto) {
        return atencionClienteRepository.findById(id)
                .map(existing -> {
                    existing.setTema(dto.getTema());
                    existing.setDescripcion(dto.getDescripcion());
                    existing.setRespuesta(dto.getRespuesta());
                    existing.setEstado(dto.getEstado());
                    AtencionCliente actualizado = atencionClienteRepository.save(existing);
                    return convertToDto(actualizado);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return atencionClienteRepository.findById(id)
                .map(ticket -> {
                    atencionClienteRepository.delete(ticket);
                    return true;
                })
                .orElse(false);
    }

    private AtencionClienteDto convertToDto(AtencionCliente ticket) {
        AtencionClienteDto dto = modelMapper.map(ticket, AtencionClienteDto.class);
        if (ticket.getUsuario() != null) {
            dto.setUsuarioId(ticket.getUsuario().getId().intValue());
            dto.setEmailUsuario(ticket.getUsuario().getEmail());
        }
        return dto;
    }
}
