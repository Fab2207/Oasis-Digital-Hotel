package com.gestion.hotelera.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (esPeticionApi(request)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Datos inválidos");
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + obtenerReferer(request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (esPeticionApi(request)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Acceso denegado");
            error.put("message", "No tienes permisos para realizar esta acción");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        redirectAttributes.addFlashAttribute("errorMessage", "No tienes permisos para acceder a esta sección.");
        return "redirect:" + obtenerReferer(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (esPeticionApi(request)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de integridad de datos");
            error.put("message", "Los datos proporcionados violan las restricciones de la base de datos");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        redirectAttributes.addFlashAttribute("errorMessage",
                "Error al procesar los datos. Verifica que no estén duplicados.");
        return "redirect:" + obtenerReferer(request);
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (esPeticionApi(request)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno");
            error.put("message", ex.getMessage() != null ? ex.getMessage() : "Ha ocurrido un error inesperado");
            error.put("type", ex.getClass().getSimpleName());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
        ex.printStackTrace(); // Log para servidor
        redirectAttributes.addFlashAttribute("errorMessage", "Ha ocurrido un error inesperado: " + ex.getMessage());
        return "redirect:" + obtenerReferer(request);
    }

    @ExceptionHandler(ClienteConReservasActivasException.class)
    public String handleClienteConReservasActivas(ClienteConReservasActivasException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        redirectAttributes.addFlashAttribute("reservasBloqueo", ex.getReservasActivas());

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/clientes/historial")) {
            return "redirect:/clientes/historial?id=" + ex.getClienteId();
        }
        return "redirect:/clientes/historial";
    }

    private boolean esPeticionApi(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains("application/json")) || request.getRequestURI().startsWith("/api/");
    }

    private String obtenerReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "/index";
    }
}