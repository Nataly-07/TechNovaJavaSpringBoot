package com.technova.technov.service.security;

import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Servicio personalizado para cargar usuarios desde la base de datos
 * e integrarlos con Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar usuario por email, verificando que esté activo
        Usuario usuario = usuarioRepository.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario activo con el email: " + email));
        
        // Debug: verificar que el usuario existe y está activo
        System.out.println("Usuario encontrado: " + usuario.getEmail() + " - Estado: " + usuario.getEstado() + " - Rol: " + usuario.getRole());

        String storedPassword = usuario.getPassword();
        
        // Si la contraseña está en texto plano, codificarla automáticamente
        boolean isEncoded = storedPassword != null && 
                           (storedPassword.startsWith("$2a$") || 
                            storedPassword.startsWith("$2b$") || 
                            storedPassword.startsWith("$2y$"));
        
        if (!isEncoded && storedPassword != null) {
            // Migrar automáticamente la contraseña a BCrypt
            String encodedPassword = passwordEncoder.encode(storedPassword);
            usuario.setPassword(encodedPassword);
            usuarioRepository.save(usuario);
            storedPassword = encodedPassword;
        }

        // Convertir el rol a formato ROLE_XXX para Spring Security
        // Los roles en la BD están en minúsculas: "admin", "cliente", "empleado"
        String role = usuario.getRole() != null ? usuario.getRole().trim().toUpperCase() : "CLIENTE";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        // Debug: imprimir el rol asignado (puedes eliminar esto después)
        System.out.println("Usuario: " + usuario.getEmail() + " - Rol asignado: " + role);

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                storedPassword != null ? storedPassword : "",
                usuario.getEstado() != null && usuario.getEstado(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}

