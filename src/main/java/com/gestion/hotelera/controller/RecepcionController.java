package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recepcion")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
public class RecepcionController {

    private final ReservaService reservaService;
    private final com.gestion.hotelera.service.NotificacionService notificacionService;

    public RecepcionController(ReservaService reservaService,
            com.gestion.hotelera.service.NotificacionService notificacionService) {
        this.reservaService = reservaService;
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public String mostrarPanelRecepcion(Model model, org.springframework.security.core.Authentication auth) {
        // Información del usuario actual
        model.addAttribute("usuarioActual", auth.getName());

        // Listas operativas para el recepcionista
        List<Reserva> llegadas = reservaService.obtenerLlegadasHoy().stream()
                .filter(r -> !"PROCESANDO".equals(r.getEstadoReserva()))
                .toList();
        List<Reserva> salidas = reservaService.obtenerSalidasHoy();

        model.addAttribute("llegadas", llegadas);
        model.addAttribute("salidas", salidas);

        // Contador de notificaciones no leídas
        long notificacionesNoLeidas = notificacionService.obtenerNoLeidas().size();
        model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
        model.addAttribute("notificaciones", notificacionService.obtenerTodas());

        return "recepcion";
    }
}
