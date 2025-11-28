package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link Compra}.
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {
    // Métodos sin filtro deleted ya que la columna no existe en la BD
    java.util.List<Compra> findAll();
    java.util.List<Compra> findByEstadoNot(String estado);
}
