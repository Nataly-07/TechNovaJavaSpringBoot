package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    List<Envio> findByVenta_Id(Integer ventaId);
    List<Envio> findByEstadoTrue();
    java.util.Optional<Envio> findByIdAndEstadoTrue(Integer id);
    List<Envio> findByVenta_IdAndEstadoTrue(Integer ventaId);
}
