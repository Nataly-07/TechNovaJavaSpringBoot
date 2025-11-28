package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link Caracteristica}.
 *
 * Proporciona consultas para obtener listados de categorías y marcas distintas
 * registradas en las características de productos.
 */
@Repository
public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Integer> {
    @Query("select distinct c.categoria from Caracteristica c where c.estado = true order by c.categoria asc")
    java.util.List<String> listarCategorias();

    @Query("select distinct c.marca from Caracteristica c where c.estado = true order by c.marca asc")
    java.util.List<String> listarMarcas();
    java.util.List<Caracteristica> findByEstadoTrue();
    java.util.Optional<Caracteristica> findByIdAndEstadoTrue(Integer id);
}
