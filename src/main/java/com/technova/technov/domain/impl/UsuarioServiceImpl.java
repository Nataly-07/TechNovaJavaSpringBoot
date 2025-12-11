package com.technova.technov.domain.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/*     @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuarios() { */

/*         // Obtener todos los admins y empleados (activos e inactivos)
        List<Usuario> admins = usuarioRepository.findByRoleIgnoreCase("admin");
        List<Usuario> empleados = usuarioRepository.findByRoleIgnoreCase("empleado");
        
        // Obtener solo clientes activos
        List<Usuario> clientesActivos = usuarioRepository.findByEstadoTrue().stream()
                .filter(u -> "cliente".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        
        // Combinar todas las listas
        List<Usuario> todosUsuarios = new java.util.ArrayList<>();
        todosUsuarios.addAll(admins);
        todosUsuarios.addAll(empleados);
        todosUsuarios.addAll(clientesActivos);
        
        return todosUsuarios.stream()
                .map(usuario -> {
                    UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
                    dto.setEstado(usuario.getEstado());
                    return dto;
                })

        List<Usuario> usuarios = usuarioRepository.findByEstadoTrue();
        return usuarios.stream()
                .sorted((a, b) -> {
                    // Ordenar por ID descendente (más reciente primero)
                    return b.getId().compareTo(a.getId());
                })
                .map(usuario -> modelMapper.map(usuario, UsuarioDto.class))
 (Cambios en Perfil de Empleado)
                .collect(Collectors.toList()); 
    }*/

                @Override
@Transactional(readOnly = true)
public List<UsuarioDto> listarUsuarios() {
    // Obtener todos los admins y empleados (activos e inactivos)
    List<Usuario> admins = usuarioRepository.findByRoleIgnoreCase("admin");
    List<Usuario> empleados = usuarioRepository.findByRoleIgnoreCase("empleado");

    // Obtener solo clientes activos
    List<Usuario> clientesActivos = usuarioRepository.findByEstadoTrue().stream()
            .filter(u -> "cliente".equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());

    // Combinar todas las listas
    List<Usuario> todosUsuarios = new java.util.ArrayList<>();
    todosUsuarios.addAll(admins);
    todosUsuarios.addAll(empleados);
    todosUsuarios.addAll(clientesActivos);

    // Ordenar por ID descendente
    todosUsuarios.sort((a, b) -> b.getId().compareTo(a.getId()));

    // Mapear a DTO y devolver la lista
    return todosUsuarios.stream()
            .map(usuario -> {
                UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
                dto.setEstado(usuario.getEstado());
                return dto;
            })
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
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
                    dto.setEstado(usuario.getEstado());
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDto> usuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
                    dto.setEstado(usuario.getEstado());
                    return dto;
                });
    }

    @Override
    @Transactional
    public UsuarioDto actualizarUsuario(Long idusuario, UsuarioDto usuarioDto) {
        return usuarioRepository.findById(idusuario)
                .map(existing -> {
                    // Solo actualizar el rol si viene en el DTO
                    if (usuarioDto.getRole() != null) {
                        existing.setRole(usuarioDto.getRole());
                    }
                    // No actualizar otros campos si no vienen en el DTO
                    Usuario actualizado = usuarioRepository.save(existing);
                    UsuarioDto dto = modelMapper.map(actualizado, UsuarioDto.class);
                    dto.setEstado(actualizado.getEstado());
                    return dto;
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public UsuarioDto actualizarPerfil(Long idusuario, UsuarioDto usuarioDto) {
        return usuarioRepository.findById(idusuario)
                .map(existing -> {
                    // Actualizar campos del perfil
                    if (usuarioDto.getName() != null) {
                        existing.setName(usuarioDto.getName());
                    }
                    if (usuarioDto.getFirstName() != null) {
                        existing.setFirstName(usuarioDto.getFirstName());
                    }
                    if (usuarioDto.getLastName() != null) {
                        existing.setLastName(usuarioDto.getLastName());
                    }
                    if (usuarioDto.getEmail() != null) {
                        existing.setEmail(usuarioDto.getEmail());
                    }
                    if (usuarioDto.getDocumentType() != null) {
                        existing.setDocumentType(usuarioDto.getDocumentType());
                    }
                    if (usuarioDto.getDocumentNumber() != null) {
                        existing.setDocumentNumber(usuarioDto.getDocumentNumber());
                    }
                    if (usuarioDto.getPhone() != null) {
                        existing.setPhone(usuarioDto.getPhone());
                    }
                    if (usuarioDto.getAddress() != null) {
                        existing.setAddress(usuarioDto.getAddress());
                    }
                    // Actualizar contraseña solo si se proporciona una nueva
                    if (usuarioDto.getPassword() != null && !usuarioDto.getPassword().trim().isEmpty()) {
                        String encodedPassword = passwordEncoder.encode(usuarioDto.getPassword());
                        existing.setPassword(encodedPassword);
                    }
                    // No actualizar el rol en la actualización de perfil
                    
                    Usuario actualizado = usuarioRepository.save(existing);
                    UsuarioDto dto = modelMapper.map(actualizado, UsuarioDto.class);
                    dto.setEstado(actualizado.getEstado());
                    return dto;
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
    public boolean activarDesactivarUsuario(Long idusuario, boolean activar) {
        return usuarioRepository.findById(idusuario)
                .map(usuario -> {
                    usuario.setEstado(activar); 
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
