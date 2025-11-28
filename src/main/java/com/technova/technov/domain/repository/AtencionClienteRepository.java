package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.AtencionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link AtencionCliente}.
 *
 * Proporciona consultas para obtener solicitudes por usuario y por estado.
 */
@Repository
public interface AtencionClienteRepository extends JpaRepository<AtencionCliente, Integer> {
    /**
     * Lista las atenciones registradas para el usuario indicado.
     *
     * @param usuarioId identificador del usuario
     * @return lista de atenciones del usuario
     */
    List<AtencionCliente> findByUsuario_Id(Long usuarioId);
    /**
     * Lista las atenciones filtrando por estado (p. ej., "abierto", "cerrado").
     *
     * @param estado estado de la atención
     * @return lista de atenciones con el estado solicitado
     */
    List<AtencionCliente> findByEstadoIgnoreCase(String estado);
    List<AtencionCliente> findByDeletedFalse();
    java.util.Optional<AtencionCliente> findByIdAndDeletedFalse(Integer id);
    List<AtencionCliente> findByUsuario_IdAndDeletedFalse(Long usuarioId);
    List<AtencionCliente> findByEstadoIgnoreCaseAndDeletedFalse(String estado);
}
