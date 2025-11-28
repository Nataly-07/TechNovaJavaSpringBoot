package com.technova.technov.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.technova.technov.domain.entity.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    java.util.List<Usuario> findByEstadoTrue();
    java.util.Optional<Usuario> findByIdAndEstadoTrue(Long id);
    Optional<Usuario> findByEmailAndPasswordAndEstadoTrue(String email, String password);
    Optional<Usuario> findByEmailAndEstadoTrue(String email);
}
