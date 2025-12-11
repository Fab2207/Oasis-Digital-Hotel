package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.ConfiguracionGlobal;
import com.gestion.hotelera.service.ConfiguracionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ConfiguracionApiController {

    private final ConfiguracionService configuracionService;

    @GetMapping("/public")
    public ResponseEntity<ConfiguracionGlobal> getPublicConfig() {
        return ResponseEntity.ok(configuracionService.obtenerConfiguracion());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ConfiguracionGlobal> getConfigAdmin() {
        return ResponseEntity.ok(configuracionService.obtenerConfiguracion());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionGlobal> updateConfig(@RequestBody ConfiguracionGlobal config) {
        return ResponseEntity.ok(configuracionService.guardarConfiguracion(config));
    }
}
