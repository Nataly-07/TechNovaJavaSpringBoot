package com.technova.technov.domain.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technova.technov.domain.dto.ProveedorDto;
import com.technova.technov.domain.entity.Producto;
import com.technova.technov.domain.entity.Proveedor;
import com.technova.technov.domain.repository.ProductoRepository;
import com.technova.technov.domain.repository.ProveedorRepository;
import com.technova.technov.domain.service.ProveedorService;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDto> listarProveedores() {
        List<Proveedor> proveedores = proveedorRepository.findByEstadoTrue();
        return proveedores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProveedorDto crearProveedor(ProveedorDto proveedorDto) {
        Proveedor entity = convertToEntity(proveedorDto);
        entity.setId(null);
        entity.setEstado(true); // true = activo
        Proveedor saved = proveedorRepository.save(entity);
        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProveedorDto> proveedorPorId(Integer idProveedor) {
        return proveedorRepository.findByIdAndEstadoTrue(idProveedor)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public ProveedorDto actualizarProveedor(Integer idProveedor, ProveedorDto proveedorDto) {
        return proveedorRepository.findByIdAndEstadoTrue(idProveedor)
                .map(existing -> {
                    existing.setIdentificacion(proveedorDto.getIdentificacion());
                    existing.setNombre(proveedorDto.getNombre());
                    existing.setTelefono(proveedorDto.getTelefono());
                    existing.setCorreo(proveedorDto.getCorreo());
                    existing.setEmpresa(proveedorDto.getEmpresa());
                    if (proveedorDto.getProductoId() != null) {
                        Producto producto = productoRepository.findByIdAndEstadoTrue(proveedorDto.getProductoId()).orElse(null);
                        existing.setProducto(producto);
                    }
                    Proveedor updated = proveedorRepository.save(existing);
                    return convertToDto(updated);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminarProveedor(Integer idProveedor) {
        return proveedorRepository.findById(idProveedor)
                .map(proveedor -> {
                    proveedor.setEstado(false);
                    proveedorRepository.save(proveedor);
                    return true;
                })
                .orElse(false);
    }

    private ProveedorDto convertToDto(Proveedor proveedor) {
        ProveedorDto dto = modelMapper.map(proveedor, ProveedorDto.class);
        if (proveedor.getProducto() != null) {
            dto.setProductoId(proveedor.getProducto().getId());
        }
        return dto;
    }

    private Proveedor convertToEntity(ProveedorDto dto) {
        Proveedor proveedor = modelMapper.map(dto, Proveedor.class);
        if (dto.getProductoId() != null) {
            Producto producto = productoRepository.findById(dto.getProductoId()).orElse(null);
            proveedor.setProducto(producto);
        }
        return proveedor;
    }
}
