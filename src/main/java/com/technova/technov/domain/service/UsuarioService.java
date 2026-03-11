package com.technova.technov.domain.service;

import java.util.List;
import java.util.Optional;

import com.technova.technov.domain.dto.UsuarioDto;

public interface UsuarioService {
    List<UsuarioDto> listarUsuarios();
    UsuarioDto crearUsuario(UsuarioDto usuarioDto);
    Optional<UsuarioDto> usuarioPorId(Integer id);
    Optional<UsuarioDto> usuarioPorEmail(String email);
    UsuarioDto actualizarUsuario(Integer idusuario, UsuarioDto usuarioDto);
    UsuarioDto actualizarPerfil(Integer idusuario, UsuarioDto usuarioDto);
    boolean eliminarUsuario(Integer idusuario);
    boolean activarDesactivarUsuario(Integer idusuario, boolean activar);
    Optional<UsuarioDto> login(String email, String password);
    boolean validarPassword(Integer usuarioId, String password);
    boolean verificarIdentidad(String email, String documentType, String documentNumber, String phone);
    boolean recuperarContrasena(String email, String newPassword);
}
