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
        Usuario usuario = usuarioRepository.findByIdAndEstadoTrue(Long.valueOf(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        AtencionCliente ticket = new AtencionCliente();
        ticket.setUsuario(usuario);
        ticket.setFechaConsulta(LocalDateTime.now());
        ticket.setTema(tema);
        ticket.setDescripcion(descripcion);
        ticket.setEstado("abierto");
        ticket.setDeleted(false);
        return convertToDto(atencionClienteRepository.save(ticket));
    }

    @Override
    @Transactional
    public AtencionClienteDto responder(Integer id, String respuesta) {
        AtencionCliente t = atencionClienteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        t.setRespuesta(respuesta);
        t.setEstado("en_proceso");
        return convertToDto(atencionClienteRepository.save(t));
    }

    @Override
    @Transactional
    public AtencionClienteDto cerrar(Integer id) {
        AtencionCliente t = atencionClienteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + id));
        t.setEstado("resuelto");
        return convertToDto(atencionClienteRepository.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtencionClienteDto> listarPorUsuario(Integer usuarioId) {
        List<AtencionCliente> tickets = atencionClienteRepository.findByUsuario_IdAndDeletedFalse(Long.valueOf(usuarioId));
        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtencionClienteDto> listarPorEstado(String estado) {
        List<AtencionCliente> tickets = atencionClienteRepository.findByEstadoIgnoreCaseAndDeletedFalse(estado);
        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AtencionClienteDto detalle(Integer id) {
        return atencionClienteRepository.findByIdAndDeletedFalse(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public AtencionClienteDto actualizar(Integer id, AtencionClienteDto dto) {
        return atencionClienteRepository.findByIdAndDeletedFalse(id)
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
                    ticket.setDeleted(true);
                    atencionClienteRepository.save(ticket);
                    return true;
                })
                .orElse(false);
    }

    private AtencionClienteDto convertToDto(AtencionCliente ticket) {
        AtencionClienteDto dto = modelMapper.map(ticket, AtencionClienteDto.class);
        if (ticket.getUsuario() != null) {
            dto.setUsuarioId(ticket.getUsuario().getId().intValue());
        }
        return dto;
    }
}
