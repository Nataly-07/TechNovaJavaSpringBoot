package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link com.technova.technov.domain.entity.Favorito}.
 *
 * Proporciona consultas para listar y localizar favoritos de un usuario sobre productos específicos.
 */
@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    /**
     * Obtiene todos los favoritos asociados al usuario indicado.
     *
     * @param userId identificador del usuario
     * @return lista de favoritos del usuario
     */
    List<Favorito> findByUsuario_Id(Long userId);
    /**
     * Busca si existe un favorito para un usuario y producto concretos.
     *
     * @param userId identificador del usuario
     * @param productoId identificador del producto
     * @return favorito, si existe
     */
    Optional<Favorito> findByUsuario_IdAndProducto_Id(Long userId, Integer productoId);
}
