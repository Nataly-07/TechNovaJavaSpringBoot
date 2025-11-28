package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de acceso a datos para {@link Venta}.
 *
 * Proporciona consultas por usuario y por rango de fechas.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    /**
     * Lista ventas registradas para el usuario indicado.
     *
     * @param usuarioId identificador del usuario
     * @return lista de ventas del usuario
     */
    List<Venta> findByUsuario_Id(Long usuarioId);
    /**
     * Lista ventas dentro del rango de fechas [desde, hasta].
     *
     * @param desde fecha inicial (inclusive)
     * @param hasta fecha final (inclusive)
     * @return lista de ventas en el rango
     */
    List<Venta> findByFechaVentaBetween(LocalDate desde, LocalDate hasta);
    List<Venta> findByEstadoTrue();
    java.util.Optional<Venta> findByIdAndEstadoTrue(Integer id);
    List<Venta> findByUsuario_IdAndEstadoTrue(Long usuarioId);
    List<Venta> findByFechaVentaBetweenAndEstadoTrue(LocalDate desde, LocalDate hasta);
}
