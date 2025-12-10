package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link Producto}.
 *
 * Proporciona consultas derivadas para filtrar por características (categoría, marca),
 * búsqueda por nombre o descripción y rangos de precio con paginación.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    /**
     * Lista productos por categoría (case-insensitive).
     * @param categoria categoría a filtrar
     * @return lista de productos
     */
    java.util.List<Producto> findByCaracteristica_CategoriaIgnoreCase(String categoria);
    /**
     * Lista productos por marca (case-insensitive).
     * @param marca marca a filtrar
     * @return lista de productos
     */
    java.util.List<Producto> findByCaracteristica_MarcaIgnoreCase(String marca);
    /**
     * Busca productos por nombre o por descripción de características (case-insensitive), paginado.
     * @param q1 término para el nombre
     * @param q2 término para la descripción de características
     * @param pageable información de paginación
     * @return página de resultados
     */
    Page<Producto> findByNombreContainingIgnoreCaseOrCaracteristica_DescripcionContainingIgnoreCase(String q1, String q2, Pageable pageable);
    /**
     * Lista productos dentro de un rango de precio de venta, paginado.
     * @param min precio mínimo
     * @param max precio máximo
     * @param pageable información de paginación
     * @return página de resultados
     */
    Page<Producto> findByCaracteristica_PrecioVentaBetween(java.math.BigDecimal min, java.math.BigDecimal max, Pageable pageable);
    java.util.List<Producto> findByEstadoTrue();
    java.util.Optional<Producto> findByIdAndEstadoTrue(Integer id);
    java.util.List<Producto> findByCaracteristica_CategoriaIgnoreCaseAndEstadoTrue(String categoria);
    java.util.List<Producto> findByCaracteristica_MarcaIgnoreCaseAndEstadoTrue(String marca);
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Producto p WHERE p.estado = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR (p.caracteristica IS NOT NULL AND LOWER(p.caracteristica.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))))")
    Page<Producto> buscarProductosNoEliminados(@org.springframework.data.repository.query.Param("q") String q, Pageable pageable);
    Page<Producto> findByCaracteristica_PrecioVentaBetweenAndEstadoTrue(java.math.BigDecimal min, java.math.BigDecimal max, Pageable pageable);
    
    /**
     * Obtiene los productos más recientes ordenados por ID descendente (los últimos creados).
     * @param pageable información de paginación
     * @return página de productos ordenados por ID descendente
     */
    Page<Producto> findByEstadoTrueOrderByIdDesc(Pageable pageable);
}
