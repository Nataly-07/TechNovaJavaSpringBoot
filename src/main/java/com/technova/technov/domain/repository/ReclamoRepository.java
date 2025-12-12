package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {
    List<Reclamo> findByUsuario_IdOrderByFechaReclamoDesc(Long usuarioId);
    List<Reclamo> findByEstadoIgnoreCaseOrderByFechaReclamoDesc(String estado);
    List<Reclamo> findAllByOrderByFechaReclamoDesc();
    List<Reclamo> findByEnviadoAlAdminTrueOrderByFechaReclamoDesc();
    
    /**
     * Busca un reclamo por ID cargando el usuario (JOIN FETCH para evitar lazy loading).
     *
     * @param id identificador del reclamo
     * @return reclamo con usuario cargado
     */
    @Query("SELECT r FROM Reclamo r LEFT JOIN FETCH r.usuario WHERE r.id = :id")
    Optional<Reclamo> findByIdWithUsuario(@Param("id") Integer id);
}

