package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.DetalleCompra;
import com.technova.technov.domain.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link DetalleCompra}.
 *
 * Permite consultar los detalles asociados a una compra.
 */
@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Integer> {
    /**
     * Lista los detalles pertenecientes a la compra indicada.
     *
     * @param compra entidad de compra
     * @return lista de detalles de la compra
     */
    List<DetalleCompra> findByCompra(Compra compra);
}
