package com.technova.technov.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado para redirigir según el rol después del login exitoso.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        try {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("ROLE_CLIENTE");
            
            // Debug: imprimir el rol detectado (puedes eliminar esto después)
            System.out.println("Login exitoso - Rol detectado: " + role);
            
            // Determinar la URL de redirección según el rol
            String redirectUrl = "/";
            if (role.contains("ADMIN")) {
                redirectUrl = "/admin/perfil";
            } else if (role.contains("EMPLEADO")) {
                redirectUrl = "/empleado/perfil";
            } else {
                // Cliente va al index autenticado
                redirectUrl = "/";
            }
            
            System.out.println("Redirigiendo a: " + redirectUrl);
            
            // Asegurar que la respuesta no esté ya comprometida
            if (!response.isCommitted()) {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            System.err.println("Error en onAuthenticationSuccess: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, redirigir a la página principal
            if (!response.isCommitted()) {
                response.sendRedirect("/?login=success");
            }
        }
    }
}




