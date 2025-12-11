package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.AtencionClienteDto;
import com.technova.technov.domain.entity.AtencionCliente;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.AtencionClienteRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.AtencionClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AtencionCliente t = atencionClienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        t.setRespuesta(respuesta);
        t.setEstado("en_proceso");
        return convertToDto(atencionClienteRepository.save(t));
    }

    @Override
    @Transactional
    public AtencionClienteDto cerrar(Integer id) {
        AtencionCliente t = atencionClienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        t.setEstado("resuelto");
        return convertToDto(atencionClienteRepository.save(t));
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
