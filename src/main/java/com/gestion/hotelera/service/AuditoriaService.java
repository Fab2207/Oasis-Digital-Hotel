package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Auditoria;
import com.gestion.hotelera.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @org.springframework.scheduling.annotation.Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAccion(String tipoAccion, String detalleAccion, String entidadAfectada,
            Long entidadAfectadaId) {
        registrarAccion(null, tipoAccion, detalleAccion, entidadAfectada, entidadAfectadaId);
    }

    @org.springframework.scheduling.annotation.Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAccion(String username, String tipoAccion, String detalleAccion, String entidadAfectada,
            Long entidadAfectadaId) {
        if (tipoAccion == null || tipoAccion.trim().isEmpty()) {
            return;
        }

        String currentUsername = username;
        if (currentUsername == null) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !(authentication.getPrincipal() instanceof String)) {
                    currentUsername = authentication.getName();
                } else if (authentication != null && authentication.isAuthenticated()
                        && (authentication.getPrincipal() instanceof String)) {
                    currentUsername = authentication.getName();
                }
            } catch (Exception e) {
                currentUsername = "Sistema/Error";
            }
        }

        Auditoria logEntry = new Auditoria();
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setUsuarioUsername(
                currentUsername != null && !currentUsername.equals("anonymousUser") ? currentUsername : "SYSTEM");
        logEntry.setTipoAccion(tipoAccion);
        logEntry.setDetalleAccion(detalleAccion);
        logEntry.setEntidadAfectada(entidadAfectada);
        logEntry.setEntidadAfectadaId(entidadAfectadaId);

        auditoriaRepository.save(logEntry);
    }

    public Page<Auditoria> obtenerTodosLosLogs(Pageable pageable) {
        if (pageable == null) {
            pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        }
        return auditoriaRepository.findAll(pageable);
    }

    public Page<Auditoria> obtenerLogsPorDniEmpleado(String dni, Pageable pageable) {
        if (dni == null || dni.trim().isEmpty()) {
            return Page.empty();
        }
        try {
            return auditoriaRepository.findByUsuarioUsernameContainingIgnoreCase(dni.trim(), pageable);
        } catch (Exception e) {
            return Page.empty();
        }
    }

    public Page<Auditoria> searchLogs(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return obtenerTodosLosLogs(pageable);
        }
        try {
            String sanitizedKeyword = keyword.trim().substring(0, Math.min(keyword.trim().length(), 100));
            return auditoriaRepository.findByTipoAccionContainingIgnoreCaseOrDetalleAccionContainingIgnoreCase(
                    sanitizedKeyword, sanitizedKeyword, pageable);
        } catch (Exception e) {
            return Page.empty();
        }
    }

    public Page<Auditoria> filtrarLogs(String tipoAccion, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin,
            Pageable pageable) {
        LocalDateTime start = (fechaInicio != null) ? fechaInicio.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = (fechaFin != null) ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        if (tipoAccion != null && !tipoAccion.trim().isEmpty()) {
            return auditoriaRepository.findByTipoAccionContainingIgnoreCaseAndTimestampBetween(tipoAccion, start, end,
                    pageable);
        } else {
            return auditoriaRepository.findByTimestampBetween(start, end, pageable);
        }
    }
}