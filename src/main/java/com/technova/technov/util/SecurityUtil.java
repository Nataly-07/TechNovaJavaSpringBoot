package com.technova.technov.util;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utilidad para obtener información del usuario autenticado desde Spring Security.
 */
@Component
public class SecurityUtil {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Obtiene el UsuarioDto del usuario autenticado actual.
     * @return Optional con el UsuarioDto si está autenticado, vacío si no.
     */
    public Optional<UsuarioDto> getUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }
            
            String email = authentication.getName();
            return usuarioRepository.findByEmailAndEstadoTrue(email)
                    .map(usuario -> modelMapper.map(usuario, UsuarioDto.class));
        } catch (Exception e) {
            // Si hay algún error (por ejemplo, Spring Security no está configurado aún),
            // retornar vacío en lugar de lanzar excepción
            return Optional.empty();
        }
    }

    /**
     * Obtiene el email del usuario autenticado.
     * @return Email del usuario o null si no está autenticado.
     */
    public String getEmailAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }
            return authentication.getName();
        } catch (Exception e) {
            return null;
        }
    }
}

