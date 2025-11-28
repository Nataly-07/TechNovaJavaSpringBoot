package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Carrito}.
 *
 * Proporciona consulta del primer carrito asociado a un usuario.
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    /**
     * Obtiene el primer carrito registrado para el usuario indicado.
     *
     * @param usuarioId identificador del usuario
     * @return carrito si existe
     */
    Optional<Carrito> findFirstByUsuario_Id(Long usuarioId);
}
