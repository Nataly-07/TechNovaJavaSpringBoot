package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link UserPaymentMethod}.
 *
 * Proporciona consulta de métodos de pago por usuario.
 */
@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Long> {
    /**
     * Lista los métodos de pago asociados a un usuario.
     *
     * @param usuarioId identificador del usuario
     * @return lista de métodos de pago del usuario
     */
    List<UserPaymentMethod> findByUsuario_Id(Long usuarioId);
}
