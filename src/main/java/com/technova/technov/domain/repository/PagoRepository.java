package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link Pago}.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
}
