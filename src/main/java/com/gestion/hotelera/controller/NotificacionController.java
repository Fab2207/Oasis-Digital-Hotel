package com.gestion.hotelera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notificaciones")
// @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA',
// 'ROLE_CLIENTE')")
public class NotificacionController {

    private final com.gestion.hotelera.service.NotificacionService notificacionService;

    public NotificacionController(com.gestion.hotelera.service.NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public String mostrarPanelNotificaciones(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String filtro,
            Model model,
            org.springframework.security.core.Authentication auth) {

        if (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        }
        if ("no-leidas".equals(filtro)) {
            model.addAttribute("notificaciones", notificacionService.obtenerNoLeidas());
            model.addAttribute("filtroActual", "no-leidas");
        } else if ("archivadas".equals(filtro)) {
            model.addAttribute("notificaciones", notificacionService.obtenerArchivadas());
            model.addAttribute("filtroActual", "archivadas");
        } else {
            model.addAttribute("notificaciones", notificacionService.obtenerTodas());
            model.addAttribute("filtroActual", "todas");
        }
        // Contador de notificaciones no leídas para el badge
        long notificacionesNoLeidas = notificacionService.obtenerNoLeidas().size();
        model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
        return "notificaciones";
    }

    @GetMapping("/ver/{id}")
    public String verNotificacion(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        notificacionService.marcarComoLeida(id);
        return "redirect:/notificaciones";
    }

    @GetMapping("/archivar/{id}")
    public String archivarNotificacion(@org.springframework.web.bind.annotation.PathVariable Long id) {
        notificacionService.archivar(id);
        return "redirect:/notificaciones";
    }

    @GetMapping("/marcar-todas-leidas")
    public String marcarTodasLeidas() {
        notificacionService.marcarTodasComoLeidas();
        return "redirect:/notificaciones";
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/marcar-todas-leidas")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Void> marcarTodasLeidasApi() {
        notificacionService.marcarTodasComoLeidas();
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/marcar-leida/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Void> marcarLeidaApi(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return org.springframework.http.ResponseEntity.ok().build();
    }
}
