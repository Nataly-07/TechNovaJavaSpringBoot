package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.ProveedorDto;
import java.util.List;
import java.util.Optional;

public interface ProveedorService {
    List<ProveedorDto> listarProveedores();
    ProveedorDto crearProveedor(ProveedorDto proveedorDto);
    Optional<ProveedorDto> proveedorPorId(Integer idProveedor);
    ProveedorDto actualizarProveedor(Integer idProveedor, ProveedorDto proveedorDto);
    boolean eliminarProveedor(Integer idProveedor);
}
