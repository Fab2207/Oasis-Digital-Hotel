package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Auditoria;
import com.gestion.hotelera.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/auditoria")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/logs")
    public String showLogsList(
            @RequestParam(name = "dniEmpleado", required = false) String dniEmpleado,
            @RequestParam(required = false) String tipoAccion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Auditoria> logsPage;

        try {
            if (dniEmpleado != null && !dniEmpleado.trim().isEmpty()) {
                String dniSanitizado = dniEmpleado.trim().replaceAll("[^0-9]", "");
                if (dniSanitizado.length() == 8) {
                    logsPage = auditoriaService.obtenerLogsPorDniEmpleado(dniSanitizado, pageable);
                    model.addAttribute("filtroDni", dniSanitizado);
                    if (logsPage.isEmpty()) {
                        model.addAttribute("message", "No hay logs registrados");
                    }
                } else {
                    logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
                    model.addAttribute("errorMessage", "DNI inválido");
                }
            } else if (search != null && !search.trim().isEmpty()) {
                String searchSanitizado = search.trim().substring(0, Math.min(search.trim().length(), 50));
                logsPage = auditoriaService.searchLogs(searchSanitizado, pageable);
                model.addAttribute("search", searchSanitizado);
                if (logsPage.isEmpty()) {
                    model.addAttribute("message", "No se encontraron logs que coincidan con la búsqueda");
                }
            } else if ((tipoAccion != null && !tipoAccion.isEmpty()) || fechaInicio != null || fechaFin != null) {
                logsPage = auditoriaService.filtrarLogs(tipoAccion, fechaInicio, fechaFin, pageable);
                model.addAttribute("tipoAccion", tipoAccion);
                model.addAttribute("fechaInicio", fechaInicio);
                model.addAttribute("fechaFin", fechaFin);
            } else {
                logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
            }
        } catch (Exception e) {
            logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
            model.addAttribute("errorMessage", "Error al procesar la consulta");
        }

        model.addAttribute("logsPage", logsPage);
        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", logsPage.getNumber());
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("totalItems", logsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "listaLogs";
    }
}