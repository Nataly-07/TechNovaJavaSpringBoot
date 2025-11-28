package com.technova.technov.domain.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.MedioDePagoDto;
import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.entity.MedioDePago;
import com.technova.technov.domain.entity.Pago;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.DetalleVentaRepository;
import com.technova.technov.domain.repository.MedioDePagoRepository;
import com.technova.technov.domain.repository.PagoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.MedioDePagoService;

@Service
public class MedioDePagoServiceImpl implements MedioDePagoService {

    @Autowired
    private MedioDePagoRepository medioDePagoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedioDePagoDto> listar() {
        return medioDePagoRepository.findByEstadoTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedioDePagoDto obtener(Integer id) { return medioDePagoRepository.findByIdAndEstadoTrue(id).map(this::toDto).orElse(null); }

    @Override
    @Transactional
    public MedioDePagoDto guardar(MedioDePagoDto m) {
        MedioDePago entity = toEntity(m);
        MedioDePago saved = medioDePagoRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return medioDePagoRepository.findById(id)
                .map(medioDePago -> {
                    medioDePago.setEstado(false); 
                    medioDePagoRepository.save(medioDePago);
                    return true;
                })
                .orElse(false);
    }

    private MedioDePagoDto toDto(MedioDePago e) {
        if (e == null) return null;
        return MedioDePagoDto.builder()
                .id(e.getId())
                .metodoPago(e.getMetodoPago())
                .pagosId(e.getPago() != null ? e.getPago().getId() : null)
                .detalleVentasId(e.getDetalleVenta() != null ? e.getDetalleVenta().getId() : null)
                .usuarioId(e.getUsuario() != null ? e.getUsuario().getId().intValue() : null)
                .fechaDeCompra(e.getFechaDeCompra())
                .tiempoDeEntrega(e.getTiempoDeEntrega())
                .build();
    }

    private MedioDePago toEntity(MedioDePagoDto dto) {
        if (dto == null) return null;
        Pago pago = null;
        if (dto.getPagosId() != null) {
            pago = pagoRepository.findById(dto.getPagosId()).orElse(null);
        }
        DetalleVenta detalle = null;
        if (dto.getDetalleVentasId() != null) {
            detalle = detalleVentaRepository.findById(dto.getDetalleVentasId()).orElse(null);
        }
        Usuario usuario = null;
        if (dto.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(Long.valueOf(dto.getUsuarioId())).orElse(null);
        }
        MedioDePago medioDePago = new MedioDePago();
        medioDePago.setId(dto.getId());
        medioDePago.setMetodoPago(dto.getMetodoPago());
        medioDePago.setPago(pago);
        medioDePago.setDetalleVenta(detalle);
        medioDePago.setUsuario(usuario);
        medioDePago.setFechaDeCompra(dto.getFechaDeCompra());
        medioDePago.setTiempoDeEntrega(dto.getTiempoDeEntrega());
        if (dto.getId() == null) {
            medioDePago.setEstado(true); 
        }
        return medioDePago;
    }
}
