package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Auditoria;
import com.gestion.hotelera.service.AuditoriaService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class AuditoriaApiController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Auditoria>> listarLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tipoAccion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "timestamp") String sortBy) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir != null ? sortDir : "desc");
        Sort sort = Sort.by(direction, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Auditoria> logs;

        if (search != null && !search.trim().isEmpty()) {
            
            logs = auditoriaService.searchLogs(search, pageRequest);
        } else if ((tipoAccion != null && !tipoAccion.trim().isEmpty()) || fechaInicio != null) {

            logs = auditoriaService.filtrarLogs(tipoAccion, fechaInicio, null, pageRequest);
        } else {
            
            logs = auditoriaService.obtenerTodosLosLogs(pageRequest);
        }

        return ResponseEntity.ok(logs);
    }
}
