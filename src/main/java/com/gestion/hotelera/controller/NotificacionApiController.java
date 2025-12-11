package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Notificacion;
import com.gestion.hotelera.service.NotificacionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@AllArgsConstructor
public class NotificacionApiController {

    private final NotificacionService notificacionService;

    @GetMapping
    public List<Notificacion> listar(org.springframework.security.core.Authentication auth) {
        String username = auth.getName();
        boolean esStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPCIONISTA"));

        return notificacionService.obtenerPorUsuario(username, esStaff);
    }

    @GetMapping("/no-leidas")
    public List<Notificacion> listarNoLeidas(org.springframework.security.core.Authentication auth) {

        return listar(auth).stream().filter(n -> !n.isLeida()).toList();
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }
}
