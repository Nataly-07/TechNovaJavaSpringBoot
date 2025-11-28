package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link Proveedor}.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    java.util.List<Proveedor> findByEstadoTrue();
    java.util.Optional<Proveedor> findByIdAndEstadoTrue(Integer id);
}
