package com.technova.technov.domain.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.CaracteristicasDto;
import com.technova.technov.domain.entity.Caracteristica;
import com.technova.technov.domain.repository.CaracteristicaRepository;
import com.technova.technov.domain.service.CaracteristicaService;

@Service
public class CaracteristicaServiceImpl implements CaracteristicaService {

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CaracteristicasDto> listar() {
        List<Caracteristica> caracteristicas = caracteristicaRepository.findByEstadoTrue();
        return caracteristicas.stream()
                .map(caracteristica -> modelMapper.map(caracteristica, CaracteristicasDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CaracteristicasDto crear(CaracteristicasDto dto) {
        Caracteristica entity = modelMapper.map(dto, Caracteristica.class);
        entity.setId(null);
        entity.setEstado(true); // true = activo
        Caracteristica saved = caracteristicaRepository.save(entity);
        return modelMapper.map(saved, CaracteristicasDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaracteristicasDto> caracteristicaPorId(Integer id) {
        return caracteristicaRepository.findByIdAndEstadoTrue(id)
                .map(caracteristica -> modelMapper.map(caracteristica, CaracteristicasDto.class));
    }

    @Override
    @Transactional
    public CaracteristicasDto actualizar(Integer id, CaracteristicasDto dto) {
        return caracteristicaRepository.findByIdAndEstadoTrue(id)
                .map(existing -> {
                    existing.setCategoria(dto.getCategoria());
                    existing.setColor(dto.getColor());
                    existing.setDescripcion(dto.getDescripcion());
                    existing.setPrecioCompra(dto.getPrecioCompra());
                    existing.setPrecioVenta(dto.getPrecioVenta());
                    existing.setMarca(dto.getMarca());
                    Caracteristica updated = caracteristicaRepository.save(existing);
                    return modelMapper.map(updated, CaracteristicasDto.class);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        return caracteristicaRepository.findById(id)
                .map(caracteristica -> {
                    caracteristica.setEstado(false); 
                    caracteristicaRepository.save(caracteristica);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return caracteristicaRepository.listarCategorias();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarMarcas() {
        return caracteristicaRepository.listarMarcas();
    }
}
