package com.technova.technov.domain.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technova.technov.domain.dto.LoginRequestDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listarTodos() {
        List<UsuarioDto> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> obtenerPorId(@PathVariable Long id) {
        UsuarioDto usuario = usuarioService.usuarioPorId(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioDto creado = usuarioService.crearUsuario(usuarioDto);
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            String errorMessage = "No se pudo completar el registro. Por favor, verifica los datos.";

            // Detectar errores comunes y devolver mensajes claros en español
            String exceptionMsg = e.getMessage().toLowerCase();

            if (exceptionMsg.contains("duplicate") || exceptionMsg.contains("duplicado")) {
                if (exceptionMsg.contains("email")) {
                    errorMessage = "Este correo electrónico ya está registrado. Por favor, usa otro correo o inicia sesión.";
                } else if (exceptionMsg.contains("document_number") || exceptionMsg.contains("documento")) {
                    errorMessage = "Este número de documento ya está registrado. Por favor, verifica tus datos.";
                } else {
                    errorMessage = "Los datos ingresados ya existen en el sistema. Por favor, verifica tu información.";
                }
            } else if (exceptionMsg.contains("constraint") || exceptionMsg.contains("restricción")) {
                errorMessage = "Los datos ingresados ya existen en el sistema. Por favor, verifica tu información.";
            } else if (exceptionMsg.contains("validation") || exceptionMsg.contains("validación")) {
                errorMessage = "Algunos datos no son válidos. Por favor, revisa el formulario.";
            }

            // Crear respuesta de error con mensaje amigable
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", errorMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioDto> login(@RequestBody LoginRequestDto request) {
        return usuarioService.login(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDto> actualizarUsuarios(@PathVariable Long id, @RequestBody UsuarioDto usuarioDto) {
        UsuarioDto usuarioDtoActualizado = usuarioService.actualizarUsuario(id, usuarioDto);
        if (usuarioDtoActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuarioDtoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        boolean eliminarUsuario = usuarioService.eliminarUsuario(id);
        if (!eliminarUsuario) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

}
