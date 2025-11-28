package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.UsuarioLegacy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para {@link UsuarioLegacy} (esquema legado).
 */
public interface UsuarioLegacyRepository extends JpaRepository<UsuarioLegacy, Integer> {
}
