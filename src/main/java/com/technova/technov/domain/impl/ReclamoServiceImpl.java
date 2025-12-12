package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.ReclamoDto;
import com.technova.technov.domain.entity.Reclamo;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.ReclamoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.ReclamoService;
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
public class ReclamoServiceImpl implements ReclamoService {

    @Autowired
    private ReclamoRepository reclamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificacionService notificacionService;

    @Override
    @Transactional
    public ReclamoDto crearReclamo(Integer usuarioId, String titulo, String descripcion, String prioridad) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        
        Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado o inactivo: " + usuarioId));
        
        Reclamo reclamo = new Reclamo();
        reclamo.setUsuario(usuario);
        reclamo.setFechaReclamo(LocalDateTime.now());
        reclamo.setTitulo(titulo.trim());
        reclamo.setDescripcion(descripcion.trim());
        reclamo.setEstado("pendiente");
        reclamo.setPrioridad(prioridad != null && !prioridad.trim().isEmpty() ? prioridad.trim().toLowerCase() : "normal");
        reclamo.setEnviadoAlAdmin(false);
        
        Reclamo reclamoGuardado = reclamoRepository.save(reclamo);
        return convertToDto(reclamoGuardado);
    }

    @Override
    @Transactional
    public ReclamoDto responder(Integer id, String respuesta) {
        System.out.println("=== INICIANDO responder() RECLAMO ===");
        System.out.println("  -> Reclamo ID: " + id);
        
        // Usar consulta con JOIN FETCH para cargar el usuario
        Reclamo r = reclamoRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new IllegalArgumentException("Reclamo no encontrado: " + id));
        
        System.out.println("  -> Reclamo encontrado: " + (r != null ? "Sí" : "No"));
        
        // Obtener usuarioId - ahora debería estar cargado
        Long usuarioId = null;
        String titulo = r.getTitulo();
        
        if (r.getUsuario() != null) {
            usuarioId = r.getUsuario().getId();
            System.out.println("  -> Usuario cargado: Sí");
            System.out.println("  -> Usuario ID: " + usuarioId);
            System.out.println("  -> Usuario Email: " + r.getUsuario().getEmail());
        } else {
            System.err.println("  -> ERROR: r.getUsuario() es null");
        }
        
        // Si aún no tenemos el usuarioId, intentar obtenerlo del DTO después de guardar
        if (usuarioId == null) {
            r.setRespuesta(respuesta);
            r.setEstado("en_revision");
            Reclamo reclamoGuardado = reclamoRepository.save(r);
            ReclamoDto reclamoRespondido = convertToDto(reclamoGuardado);
            
            if (reclamoRespondido != null && reclamoRespondido.getUsuarioId() != null) {
                usuarioId = Long.valueOf(reclamoRespondido.getUsuarioId());
                System.out.println("  -> Usuario ID obtenido del DTO: " + usuarioId);
            }
        } else {
            r.setRespuesta(respuesta);
            r.setEstado("en_revision");
            reclamoRepository.save(r);
        }
        
        ReclamoDto reclamoRespondido = convertToDto(r);
        
        // Crear notificación para el cliente
        if (usuarioId != null) {
            try {
                System.out.println("=== CREAR NOTIFICACIÓN DE RESPUESTA A RECLAMO ===");
                System.out.println("  -> Reclamo ID: " + id);
                System.out.println("  -> Usuario ID: " + usuarioId);
                System.out.println("  -> Título: " + titulo);
                
                String mensaje = String.format(
                    "Hemos respondido a tu reclamo sobre '%s'. " +
                    "Revisa la respuesta en tu panel de reclamos.",
                    titulo != null && titulo.length() > 50 
                        ? titulo.substring(0, 50) + "..." 
                        : (titulo != null ? titulo : "tu reclamo")
                );
                
                // Crear JSON con datos adicionales
                ObjectMapper objectMapper = new ObjectMapper();
                java.util.Map<String, Object> dataAdicional = new java.util.HashMap<>();
                dataAdicional.put("reclamoId", id);
                dataAdicional.put("titulo", titulo);
                String dataAdicionalJson = objectMapper.writeValueAsString(dataAdicional);
                
                NotificacionDto notificacion = NotificacionDto.builder()
                        .userId(usuarioId)
                        .titulo("Respuesta a tu reclamo")
                        .mensaje(mensaje)
                        .tipo("reclamo")
                        .icono("bx-error-circle")
                        .leida(false)
                        .dataAdicional(dataAdicionalJson)
                        .build();
                
                NotificacionDto notificacionCreada = notificacionService.crear(notificacion);
                System.out.println("=== NOTIFICACIÓN: Notificación de respuesta a reclamo creada exitosamente ===");
                System.out.println("  -> Notificación ID: " + (notificacionCreada != null ? notificacionCreada.getId() : "null"));
            } catch (Exception e) {
                System.err.println("=== ERROR: No se pudo crear la notificación de respuesta a reclamo ===");
                System.err.println("  -> Error: " + e.getMessage());
                System.err.println("  -> Stack trace:");
                e.printStackTrace();
                // No lanzar excepción para no interrumpir el proceso
            }
        } else {
            System.err.println("=== ADVERTENCIA: No se pudo crear notificación - Usuario ID es null ===");
            System.err.println("  -> Reclamo ID: " + id);
        }
        
        return reclamoRespondido;
    }

    @Override
    @Transactional
    public ReclamoDto cerrar(Integer id) {
        Reclamo r = reclamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reclamo no encontrado: " + id));
        r.setEstado("resuelto");
        return convertToDto(reclamoRepository.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamoDto> listarPorUsuario(Integer usuarioId) {
        List<Reclamo> reclamos = reclamoRepository.findByUsuario_IdOrderByFechaReclamoDesc(Long.valueOf(usuarioId));
        return reclamos.stream()
                .sorted((r1, r2) -> {
                    if (r1.getFechaReclamo() == null && r2.getFechaReclamo() == null) return 0;
                    if (r1.getFechaReclamo() == null) return 1;
                    if (r2.getFechaReclamo() == null) return -1;
                    return r2.getFechaReclamo().compareTo(r1.getFechaReclamo());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamoDto> listarPorEstado(String estado) {
        List<Reclamo> reclamos = reclamoRepository.findByEstadoIgnoreCaseOrderByFechaReclamoDesc(estado);
        return reclamos.stream()
                .sorted((r1, r2) -> {
                    if (r1.getFechaReclamo() == null && r2.getFechaReclamo() == null) return 0;
                    if (r1.getFechaReclamo() == null) return 1;
                    if (r2.getFechaReclamo() == null) return -1;
                    return r2.getFechaReclamo().compareTo(r1.getFechaReclamo());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamoDto> listarTodos() {
        List<Reclamo> reclamos = reclamoRepository.findAllByOrderByFechaReclamoDesc();
        return reclamos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamoDto detalle(Integer id) {
        return reclamoRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public ReclamoDto actualizar(Integer id, ReclamoDto dto) {
        return reclamoRepository.findById(id)
                .map(existing -> {
                    existing.setTitulo(dto.getTitulo());
                    existing.setDescripcion(dto.getDescripcion());
                    existing.setRespuesta(dto.getRespuesta());
                    existing.setEstado(dto.getEstado());
                    existing.setPrioridad(dto.getPrioridad());
                    Reclamo actualizado = reclamoRepository.save(existing);
                    return convertToDto(actualizado);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return reclamoRepository.findById(id)
                .map(reclamo -> {
                    reclamoRepository.delete(reclamo);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public ReclamoDto enviarAlAdministrador(Integer id) {
        Reclamo r = reclamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reclamo no encontrado: " + id));
        r.setEnviadoAlAdmin(true);
        r.setEstado("enviado_al_admin");
        return convertToDto(reclamoRepository.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamoDto> listarQuejasEnviadasPorEmpleados() {
        try {
            // Intentar obtener reclamos enviados al admin
            List<Reclamo> reclamos = reclamoRepository.findByEnviadoAlAdminTrueOrderByFechaReclamoDesc();
            if (reclamos == null) {
                return new java.util.ArrayList<>();
            }
            return reclamos.stream()
                    .filter(r -> r != null && r.getEnviadoAlAdmin() != null && r.getEnviadoAlAdmin())
                    .map(this::convertToDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al listar quejas enviadas por empleados: " + e.getMessage());
            e.printStackTrace();
            // Si hay un error, retornar lista vacía en lugar de lanzar excepción
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Transactional
    public ReclamoDto evaluarResolucion(Integer id, String evaluacion) {
        Reclamo r = reclamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reclamo no encontrado: " + id));
        
        if (r.getRespuesta() == null || r.getRespuesta().trim().isEmpty()) {
            throw new IllegalArgumentException("El reclamo no tiene respuesta, no puede ser evaluado");
        }
        
        if (evaluacion == null || (!evaluacion.equalsIgnoreCase("resuelta") && !evaluacion.equalsIgnoreCase("no_resuelta"))) {
            throw new IllegalArgumentException("La evaluación debe ser 'resuelta' o 'no_resuelta'");
        }
        
        r.setEvaluacionCliente(evaluacion.toLowerCase());
        return convertToDto(reclamoRepository.save(r));
    }

    private ReclamoDto convertToDto(Reclamo reclamo) {
        if (reclamo == null) {
            return null;
        }
        try {
            ReclamoDto dto = modelMapper.map(reclamo, ReclamoDto.class);
            if (reclamo.getUsuario() != null && reclamo.getUsuario().getId() != null) {
                dto.setUsuarioId(reclamo.getUsuario().getId().intValue());
                dto.setEmailUsuario(reclamo.getUsuario().getEmail());
            }
            if (reclamo.getEnviadoAlAdmin() != null) {
                dto.setEnviadoAlAdmin(reclamo.getEnviadoAlAdmin());
            } else {
                dto.setEnviadoAlAdmin(false);
            }
            dto.setEvaluacionCliente(reclamo.getEvaluacionCliente());
            return dto;
        } catch (Exception e) {
            System.err.println("Error al convertir Reclamo a DTO: " + e.getMessage());
            e.printStackTrace();
            // Retornar un DTO básico en caso de error
            ReclamoDto dto = new ReclamoDto();
            dto.setId(reclamo.getId());
            dto.setTitulo(reclamo.getTitulo() != null ? reclamo.getTitulo() : "");
            dto.setDescripcion(reclamo.getDescripcion() != null ? reclamo.getDescripcion() : "");
            dto.setEstado(reclamo.getEstado() != null ? reclamo.getEstado() : "pendiente");
            dto.setPrioridad(reclamo.getPrioridad() != null ? reclamo.getPrioridad() : "normal");
            dto.setRespuesta(reclamo.getRespuesta());
            dto.setEnviadoAlAdmin(reclamo.getEnviadoAlAdmin() != null ? reclamo.getEnviadoAlAdmin() : false);
            dto.setEvaluacionCliente(reclamo.getEvaluacionCliente());
            if (reclamo.getUsuario() != null && reclamo.getUsuario().getId() != null) {
                dto.setUsuarioId(reclamo.getUsuario().getId().intValue());
                dto.setEmailUsuario(reclamo.getUsuario().getEmail());
            }
            dto.setFechaReclamo(reclamo.getFechaReclamo());
            return dto;
        }
    }
}

