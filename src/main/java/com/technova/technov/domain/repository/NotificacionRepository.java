package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuario_IdOrderByFechaCreacionDesc(Long userId);
    List<Notificacion> findByUsuario_IdAndLeidaOrderByFechaCreacionDesc(Long userId, boolean leida);
    List<Notificacion> findByUsuario_IdAndFechaCreacionBetween(Long userId, Instant from, Instant to);
}
