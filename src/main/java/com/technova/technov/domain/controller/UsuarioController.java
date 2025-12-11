package com.technova.technov.domain.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/verificar-identidad")
    public ResponseEntity<?> verificarIdentidad(@RequestBody java.util.Map<String, String> datos) {
        try {
            String email = datos.get("email");
            String documentType = datos.get("documentType");
            String documentNumber = datos.get("documentNumber");
            String phone = datos.get("phone");

            if (email == null || documentType == null || documentNumber == null || phone == null) {
                java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("valid", false);
                errorResponse.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean esValido = usuarioService.verificarIdentidad(email, documentType, documentNumber, phone);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("valid", esValido);
            if (!esValido) {
                response.put("message", "Los datos no coinciden con nuestros registros");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Error al verificar la identidad");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<?> recuperarContrasena(@RequestBody java.util.Map<String, String> datos) {
        try {
            String email = datos.get("email");
            String newPassword = datos.get("newPassword");

            if (email == null || email.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
                java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email y nueva contraseña son requeridos");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validar que la contraseña cumpla con los requisitos
            if (newPassword.length() < 8 || 
                !newPassword.matches(".*[A-Z].*") || 
                !newPassword.matches(".*[a-z].*") || 
                !newPassword.matches(".*\\d.*") || 
                !newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean actualizado = usuarioService.recuperarContrasena(email, newPassword);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            if (actualizado) {
                response.put("success", true);
                response.put("message", "Contraseña actualizada correctamente");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo actualizar la contraseña. Verifica que el email sea correcto.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al recuperar la contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioDto> activarDesactivarUsuario(
            @PathVariable Long id,
            @RequestParam boolean activar) {
        boolean resultado = usuarioService.activarDesactivarUsuario(id, activar);
        if (!resultado) {
            return ResponseEntity.notFound().build();
        }
        UsuarioDto usuario = usuarioService.usuarioPorId(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

}
