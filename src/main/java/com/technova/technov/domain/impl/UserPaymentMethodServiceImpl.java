package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.UserPaymentMethodDto;
import com.technova.technov.domain.entity.UserPaymentMethod;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.UserPaymentMethodRepository;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.UserPaymentMethodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPaymentMethodServiceImpl implements UserPaymentMethodService {

    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UsuarioRepository usuarioRepository;

    public UserPaymentMethodServiceImpl(UserPaymentMethodRepository userPaymentMethodRepository,
                                        UsuarioRepository usuarioRepository) {
        this.userPaymentMethodRepository = userPaymentMethodRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPaymentMethodDto> listarTodos() {
        return userPaymentMethodRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPaymentMethodDto> listarPorUsuario(Integer usuarioId) {
        return userPaymentMethodRepository.findByUsuario_Id(usuarioId.longValue())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserPaymentMethodDto guardar(Integer usuarioId, UserPaymentMethodDto upm) {
        Usuario u = usuarioRepository.findById(usuarioId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        UserPaymentMethod entity = toEntity(upm);
        entity.setUsuario(u);
        UserPaymentMethod saved = userPaymentMethodRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        userPaymentMethodRepository.deleteById(id);
    }

    private UserPaymentMethodDto toDto(UserPaymentMethod e) {
        if (e == null) return null;
        return UserPaymentMethodDto.builder()
                .id(e.getId())
                .userId(e.getUsuario() != null ? e.getUsuario().getId() : null)
                .metodoPago(e.getMetodoPago())
                .isDefault(e.isDefault())
                .brand(e.getBrand())
                .last4(e.getLast4())
                .holderName(e.getHolderName())
                .token(e.getToken())
                .expMonth(e.getExpMonth())
                .expYear(e.getExpYear())
                .email(e.getEmail())
                .phone(e.getPhone())
                .installments(e.getInstallments())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private UserPaymentMethod toEntity(UserPaymentMethodDto dto) {
        if (dto == null) return null;
        return UserPaymentMethod.builder()
                .id(dto.getId())
                .metodoPago(dto.getMetodoPago())
                .brand(dto.getBrand())
                .last4(dto.getLast4())
                .holderName(dto.getHolderName())
                .token(dto.getToken())
                .expMonth(dto.getExpMonth())
                .expYear(dto.getExpYear())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .installments(dto.getInstallments())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .isDefault(dto.isDefault())
                .build();
    }
}
