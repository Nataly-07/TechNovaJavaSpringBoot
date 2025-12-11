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
     * Lista las atenciones registradas para el usuario indicado, ordenadas por fecha descendente (más recientes primero).
     *
     * @param usuarioId identificador del usuario
     * @return lista de atenciones del usuario ordenadas por fecha descendente
     */
    List<AtencionCliente> findByUsuario_IdOrderByFechaConsultaDesc(Long usuarioId);
    /**
     * Lista las atenciones filtrando por estado (p. ej., "abierto", "cerrado").
     *
     * @param estado estado de la atención
     * @return lista de atenciones con el estado solicitado
     */
    List<AtencionCliente> findByEstadoIgnoreCase(String estado);
    
    /**
     * Lista las atenciones filtrando por estado, ordenadas por fecha descendente (más recientes primero).
     *
     * @param estado estado de la atención
     * @return lista de atenciones con el estado solicitado, ordenadas por fecha descendente
     */
    List<AtencionCliente> findByEstadoIgnoreCaseOrderByFechaConsultaDesc(String estado);
    
    /**
     * Lista todas las atenciones ordenadas por fecha descendente (más recientes primero).
     *
     * @return lista de todas las atenciones ordenadas por fecha descendente
     */
    List<AtencionCliente> findAllByOrderByFechaConsultaDesc();
}
