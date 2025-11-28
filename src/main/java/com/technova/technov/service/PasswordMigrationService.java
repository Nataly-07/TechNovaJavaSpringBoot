package com.technova.technov.service;

import com.technova.technov.domain.entity.Usuario;
import com.technova.technov.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para migrar contraseñas de texto plano a BCrypt.
 * Este servicio debe ejecutarse una vez después de implementar Spring Security.
 */
@Service
public class PasswordMigrationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Verifica si una contraseña está codificada con BCrypt.
     * Las contraseñas BCrypt siempre empiezan con $2a$, $2b$ o $2y$.
     */
    private boolean isPasswordEncoded(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
    }

    /**
     * Migra todas las contraseñas de texto plano a BCrypt.
     * @return Número de contraseñas migradas
     */
    @Transactional
    public int migrarContrasenas() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int contador = 0;

        for (Usuario usuario : usuarios) {
            String password = usuario.getPassword();
            
            // Si la contraseña no está codificada, codificarla
            if (!isPasswordEncoded(password)) {
                String passwordCodificada = passwordEncoder.encode(password);
                usuario.setPassword(passwordCodificada);
                usuarioRepository.save(usuario);
                contador++;
                System.out.println("Contraseña migrada para usuario: " + usuario.getEmail());
            }
        }

        return contador;
    }

    /**
     * Migra la contraseña de un usuario específico por email.
     * @param email Email del usuario
     * @return true si se migró, false si no se encontró o ya estaba codificada
     */
    @Transactional
    public boolean migrarContrasenaPorEmail(String email) {
        return usuarioRepository.findByEmailAndEstadoTrue(email)
                .map(usuario -> {
                    String password = usuario.getPassword();
                    if (!isPasswordEncoded(password)) {
                        String passwordCodificada = passwordEncoder.encode(password);
                        usuario.setPassword(passwordCodificada);
                        usuarioRepository.save(usuario);
                        System.out.println("Contraseña migrada para usuario: " + usuario.getEmail());
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}




