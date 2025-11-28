package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.MedioDePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link MedioDePago}.
 */
@Repository
public interface MedioDePagoRepository extends JpaRepository<MedioDePago, Integer> {
    java.util.List<MedioDePago> findByEstadoTrue();
    java.util.Optional<MedioDePago> findByIdAndEstadoTrue(Integer id);
}
