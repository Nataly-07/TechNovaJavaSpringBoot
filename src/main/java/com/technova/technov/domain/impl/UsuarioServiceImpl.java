package com.technova.technov.domain.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.UsuarioRepository;
import com.technova.technov.domain.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findByEstadoTrue();
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioDto crearUsuario(UsuarioDto personaDto) {
        Usuario nuevo = modelMapper.map(personaDto, Usuario.class);
        nuevo.setId(null);
        // Codificar la contraseña antes de guardarla
        if (personaDto.getPassword() != null && !personaDto.getPassword().isEmpty()) {
            nuevo.setPassword(passwordEncoder.encode(personaDto.getPassword()));
        }
        nuevo.setEstado(true); 
        Usuario guardado = usuarioRepository.save(nuevo);
        return modelMapper.map(guardado, UsuarioDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDto> usuarioPorId(Long id) {
        return usuarioRepository.findByIdAndEstadoTrue(id)
                .map(usuario -> modelMapper.map(usuario, UsuarioDto.class));
    }

    @Override
    @Transactional
    public UsuarioDto actualizarUsuario(Long idusuario, UsuarioDto usuarioDto) {
        return usuarioRepository.findByIdAndEstadoTrue(idusuario)
                .map(existing -> {
                    existing.setName(usuarioDto.getName());
                    existing.setEmail(usuarioDto.getEmail());
                    existing.setFirstName(usuarioDto.getFirstName());
                    existing.setLastName(usuarioDto.getLastName());
                    existing.setDocumentType(usuarioDto.getDocumentType());
                    existing.setDocumentNumber(usuarioDto.getDocumentNumber());
                    existing.setPhone(usuarioDto.getPhone());
                    existing.setAddress(usuarioDto.getAddress());
                    existing.setRole(usuarioDto.getRole());
                    if (StringUtils.hasText(usuarioDto.getPassword())) {
                        // Codificar la contraseña antes de guardarla
                        existing.setPassword(passwordEncoder.encode(usuarioDto.getPassword()));
                    }
                    Usuario actualizado = usuarioRepository.save(existing);
                    return modelMapper.map(actualizado, UsuarioDto.class);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminarUsuario(Long idusuario) {
        return usuarioRepository.findById(idusuario)
                .map(usuario -> {
                    usuario.setEstado(false); 
                    usuarioRepository.save(usuario);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public Optional<UsuarioDto> login(String email, String password) {
        return usuarioRepository.findByEmailAndEstadoTrue(email)
                .map(usuario -> {
                    String storedPassword = usuario.getPassword();
                    boolean passwordMatches = false;
                    
                    // Verificar si la contraseña está codificada con BCrypt
                    boolean isEncoded = storedPassword != null && 
                                      (storedPassword.startsWith("$2a$") || 
                                       storedPassword.startsWith("$2b$") || 
                                       storedPassword.startsWith("$2y$"));
                    
                    if (isEncoded) {
                        // Contraseña codificada: usar BCrypt para verificar
                        passwordMatches = passwordEncoder.matches(password, storedPassword);
                    } else {
                        // Contraseña en texto plano: comparar directamente
                        passwordMatches = storedPassword != null && storedPassword.equals(password);
                        
                        // Si coincide y está en texto plano, migrarla automáticamente
                        if (passwordMatches) {
                            String encodedPassword = passwordEncoder.encode(password);
                            usuario.setPassword(encodedPassword);
                            usuarioRepository.save(usuario);
                        }
                    }
                    
                    return passwordMatches ? modelMapper.map(usuario, UsuarioDto.class) : null;
                })
                .filter(dto -> dto != null);
    }
}
