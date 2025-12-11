package com.technova.technov.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.exceptions.TemplateInputException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador global de excepciones para capturar errores durante el procesamiento de templates.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({TemplateProcessingException.class, TemplateInputException.class})
    public ModelAndView handleTemplateProcessingException(HttpServletRequest request, Exception ex) {
        System.err.println("Error al procesar template Thymeleaf: " + ex.getMessage());
        System.err.println("Clase: " + ex.getClass().getName());
        if (ex instanceof TemplateProcessingException) {
            TemplateProcessingException tpe = (TemplateProcessingException) ex;
            System.err.println("Template: " + tpe.getTemplateName());
        }
        ex.printStackTrace();
        
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.contains("/cliente/atencion-cliente")) {
            return new ModelAndView("redirect:/cliente/perfil");
        }
        if (requestURI != null && requestURI.contains("/empleado/atencion-cliente")) {
            return new ModelAndView("redirect:/empleado/perfil");
        }
        
        return new ModelAndView("redirect:/login");
    }
    
    @ExceptionHandler({Exception.class})
    public ModelAndView handleGeneralException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        // Solo manejar si la respuesta no se ha comprometido
        if (!response.isCommitted()) {
            System.err.println("Error general en controlador: " + ex.getMessage());
            System.err.println("Clase: " + ex.getClass().getName());
            ex.printStackTrace();
            
            String requestURI = request.getRequestURI();
            if (requestURI != null && requestURI.contains("/cliente/atencion-cliente")) {
                return new ModelAndView("redirect:/cliente/perfil");
            }
            if (requestURI != null && requestURI.contains("/empleado/atencion-cliente")) {
                return new ModelAndView("redirect:/empleado/perfil");
            }
        }
        
        return null; // Dejar que Spring maneje el error si la respuesta ya está comprometida
    }
}

