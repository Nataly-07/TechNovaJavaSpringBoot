package com.technova.technov.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro para asegurar que las respuestas HTTP se completen correctamente.
 */
@Component
@Order(1)
public class ResponseCompletionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Aplicar a rutas que pueden tener problemas de encoding
        String requestURI = httpRequest.getRequestURI();
        if (requestURI != null && (requestURI.contains("/cliente/atencion-cliente") || 
                                   requestURI.contains("/cliente/reclamos") ||
                                   requestURI.contains("/empleado/reclamos"))) {
            try {
                // Asegurar que la respuesta tenga los headers correctos
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.setContentType("text/html;charset=UTF-8");
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Expires", "0");
                
                // Continuar con la cadena de filtros
                chain.doFilter(request, response);
                
                // Asegurar que la respuesta se complete correctamente
                if (!httpResponse.isCommitted()) {
                    httpResponse.flushBuffer();
                }
            } catch (Exception e) {
                System.err.println("Error en ResponseCompletionFilter: " + e.getMessage());
                e.printStackTrace();
                if (!httpResponse.isCommitted()) {
                    try {
                        if (requestURI.contains("/cliente/")) {
                            httpResponse.sendRedirect("/cliente/perfil");
                        } else if (requestURI.contains("/empleado/")) {
                            httpResponse.sendRedirect("/empleado/perfil");
                        } else {
                            httpResponse.sendRedirect("/login");
                        }
                    } catch (IOException ioException) {
                        System.err.println("Error al redirigir: " + ioException.getMessage());
                    }
                }
            }
        } else {
            // Para otras rutas, asegurar encoding UTF-8
            httpResponse.setCharacterEncoding("UTF-8");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se requiere inicialización
    }

    @Override
    public void destroy() {
        // No se requiere limpieza
    }
}


