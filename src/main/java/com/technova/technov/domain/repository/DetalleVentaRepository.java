package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.DetalleVenta;
import com.technova.technov.domain.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link DetalleVenta}.
 *
 * Permite consultar los detalles asociados a una venta.
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    /**
     * Lista los detalles pertenecientes a la venta indicada.
     *
     * @param venta entidad de venta
     * @return lista de detalles de la venta
     */
    List<DetalleVenta> findByVenta(Venta venta);
}
