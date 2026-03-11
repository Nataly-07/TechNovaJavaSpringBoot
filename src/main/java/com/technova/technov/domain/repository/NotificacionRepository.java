package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuario_IdOrderByFechaCreacionDesc(Integer userId);
    List<Notificacion> findByUsuario_IdAndLeidaOrderByFechaCreacionDesc(Integer userId, boolean leida);
    List<Notificacion> findByUsuario_IdAndFechaCreacionBetween(Integer userId, Instant from, Instant to);
}
