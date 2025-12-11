package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Producto}.
 *
 * Proporciona consultas derivadas para filtrar por características (categoría, marca),
 * búsqueda por nombre o descripción y rangos de precio con paginación.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByCaracteristica_CategoriaIgnoreCase(String categoria);

    List<Producto> findByCaracteristica_MarcaIgnoreCase(String marca);

    Page<Producto> findByNombreContainingIgnoreCaseOrCaracteristica_DescripcionContainingIgnoreCase(String q1, String q2, Pageable pageable);

    Page<Producto> findByCaracteristica_PrecioVentaBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    List<Producto> findByEstadoTrue();

    Optional<Producto> findByIdAndEstadoTrue(Integer id);

    List<Producto> findByCaracteristica_CategoriaIgnoreCaseAndEstadoTrue(String categoria);

    List<Producto> findByCaracteristica_MarcaIgnoreCaseAndEstadoTrue(String marca);

    @Query("SELECT p FROM Producto p WHERE p.estado = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR (p.caracteristica IS NOT NULL AND LOWER(p.caracteristica.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))))")
    Page<Producto> buscarProductosNoEliminados(@Param("q") String q, Pageable pageable);

    Page<Producto> findByCaracteristica_PrecioVentaBetweenAndEstadoTrue(BigDecimal min, BigDecimal max, Pageable pageable);

    /**
     * Obtiene los productos más recientes ordenados por ID descendente (los últimos creados).
     * @param pageable información de paginación
     * @return página de productos ordenados por ID descendente
     */
    Page<Producto> findByEstadoTrueOrderByIdDesc(Pageable pageable);

    /**
     * Lista productos activos con sus características cargadas (JOIN FETCH).
     * @return lista de productos con características cargadas
     */
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.caracteristica WHERE p.estado = true")
    List<Producto> findAllWithCaracteristicas();
}
