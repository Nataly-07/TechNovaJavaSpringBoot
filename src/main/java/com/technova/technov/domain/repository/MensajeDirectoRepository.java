package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.MensajeDirecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeDirectoRepository extends JpaRepository<MensajeDirecto, Long> {
    List<MensajeDirecto> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<MensajeDirecto> findByEmpleadoIdOrderByCreatedAtDesc(Long empleadoId);
    List<MensajeDirecto> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<MensajeDirecto> findByUserIdAndEstadoOrderByCreatedAtDesc(Long userId, String estado);
}
