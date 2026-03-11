package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.MensajeEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeEmpleadoRepository extends JpaRepository<MensajeEmpleado, Integer> {
    List<MensajeEmpleado> findByEmpleadoIdOrderByCreatedAtDesc(Integer empleadoId);
    List<MensajeEmpleado> findByEmpleadoIdAndLeidoOrderByCreatedAtDesc(Integer empleadoId, boolean leido);
    List<MensajeEmpleado> findByTipoAndPrioridadOrderByCreatedAtDesc(String tipo, String prioridad);
}
