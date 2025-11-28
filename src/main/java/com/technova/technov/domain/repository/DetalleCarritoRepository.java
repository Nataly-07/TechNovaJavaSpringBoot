package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.DetalleCarrito;
import com.technova.technov.domain.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar los detalles de un {@link Carrito}.
 *
 * <p>Permite consultar y eliminar los detalles asociados a un carrito específico.</p>
 */
@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Integer> {
    /**
     * Obtiene la lista de detalles pertenecientes al carrito indicado.
     *
     * @param carrito carrito de referencia
     * @return lista de detalles del carrito
     */
    List<DetalleCarrito> findByCarrito(Carrito carrito);
    /**
     * Elimina todos los detalles asociados al carrito indicado.
     *
     * @param carrito carrito cuyo contenido se desea vaciar
     */
    void deleteByCarrito(Carrito carrito);
}
