package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.MensajeDirecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeDirectoRepository extends JpaRepository<MensajeDirecto, Integer> {
    List<MensajeDirecto> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<MensajeDirecto> findByEmpleadoIdOrderByCreatedAtDesc(Integer empleadoId);
    List<MensajeDirecto> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<MensajeDirecto> findByUserIdAndEstadoOrderByCreatedAtDesc(Integer userId, String estado);
}
