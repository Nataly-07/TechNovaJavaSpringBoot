package com.technova.technov.domain.repository;

import com.technova.technov.domain.entity.Transportadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportadoraRepository extends JpaRepository<Transportadora, Integer> {
    List<Transportadora> findByEnvio_Id(Integer envioId);
    List<Transportadora> findByEstadoTrue();
    java.util.Optional<Transportadora> findByIdAndEstadoTrue(Integer id);
    List<Transportadora> findByEnvio_IdAndEstadoTrue(Integer envioId);
}
