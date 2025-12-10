package com.technova.technov.domain.service;

import java.util.List;
import java.util.Optional;

import com.technova.technov.domain.dto.UsuarioDto;

public interface UsuarioService {
    List<UsuarioDto> listarUsuarios();
    UsuarioDto crearUsuario(UsuarioDto usuarioDto);
    Optional<UsuarioDto> usuarioPorId(Long id);
    Optional<UsuarioDto> usuarioPorEmail(String email);
    UsuarioDto actualizarUsuario(Long idusuario, UsuarioDto usuarioDto);
    UsuarioDto actualizarPerfil(Long idusuario, UsuarioDto usuarioDto);
    boolean eliminarUsuario(Long idusuario);
    boolean activarDesactivarUsuario(Long idusuario, boolean activar);
    Optional<UsuarioDto> login(String email, String password);
}
