package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {
    List<Reclamo> findByUsuario_IdOrderByFechaReclamoDesc(Long usuarioId);
    List<Reclamo> findByEstadoIgnoreCaseOrderByFechaReclamoDesc(String estado);
    List<Reclamo> findAllByOrderByFechaReclamoDesc();
    List<Reclamo> findByEnviadoAlAdminTrueOrderByFechaReclamoDesc();
}

