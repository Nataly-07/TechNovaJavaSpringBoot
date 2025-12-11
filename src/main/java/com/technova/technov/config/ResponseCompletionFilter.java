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
        
        // Solo aplicar a la ruta problemática
        String requestURI = httpRequest.getRequestURI();
        if (requestURI != null && requestURI.contains("/cliente/atencion-cliente")) {
            try {
                // Asegurar que la respuesta tenga los headers correctos
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.setContentType("text/html;charset=UTF-8");
                
                // Continuar con la cadena de filtros
                chain.doFilter(request, response);
                
                // Asegurar que la respuesta se complete
                if (!httpResponse.isCommitted()) {
                    httpResponse.flushBuffer();
                }
            } catch (Exception e) {
                System.err.println("Error en ResponseCompletionFilter: " + e.getMessage());
                if (!httpResponse.isCommitted()) {
                    httpResponse.sendRedirect("/cliente/perfil");
                }
            }
        } else {
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

