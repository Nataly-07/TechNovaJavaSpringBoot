package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.FavoritoDto;
import com.technova.technov.domain.entity.Favorito;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.FavoritoRepository;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.FavoritoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public List<FavoritoDto> listarTodos() {
        return favoritoRepository.findAll().stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de actualización descendente (más reciente primero)
                    if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                        int fechaCompare = b.getUpdatedAt().compareTo(a.getUpdatedAt());
                        if (fechaCompare != 0) return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<FavoritoDto> listarPorUsuario(Long usuarioId) {
        List<Favorito> favoritos = favoritoRepository.findByUsuario_Id(usuarioId);
        return favoritos.stream()
                .sorted((a, b) -> {
                    // Ordenar por fecha de actualización descendente (más reciente primero)
                    if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                        int fechaCompare = b.getUpdatedAt().compareTo(a.getUpdatedAt());
                        if (fechaCompare != 0) return fechaCompare;
                    }
                    // Si las fechas son iguales o nulas, ordenar por ID descendente
                    return b.getId().compareTo(a.getId());
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public FavoritoDto agregar(Long usuarioId, Integer productoId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        Favorito fav = favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId)
                .orElse(null);
        if (fav == null) {
            fav = new Favorito();
            fav.setUsuario(u);
            fav.setProducto(p);
            fav.setCreatedAt(Instant.now());
            fav.setUpdatedAt(Instant.now());
        } else {
            fav.setUpdatedAt(Instant.now());
        }
        fav = favoritoRepository.save(fav);
        return convertToDto(fav);
    }

    @Transactional
    @Override
    public FavoritoDto eliminar(Long usuarioId, Integer productoId) {
        return favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId)
                .map(f -> {
                    FavoritoDto dto = convertToDto(f);
                    favoritoRepository.delete(f);
                    return dto;
                })
                .orElse(null);
    }

    private FavoritoDto convertToDto(Favorito favorito) {
        FavoritoDto dto = modelMapper.map(favorito, FavoritoDto.class);
        if (favorito.getUsuario() != null) {
            dto.setUsuarioId(favorito.getUsuario().getId());
        }
        if (favorito.getProducto() != null) {
            dto.setProductoId(favorito.getProducto().getId());
        }
        return dto;
    }

    @Transactional
    @Override
    public boolean toggle(Long usuarioId, Integer productoId) {
        return favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId)
                .map(f -> { favoritoRepository.delete(f); return false; })
                .orElseGet(() -> { agregar(usuarioId, productoId); return true; });
    }
}
