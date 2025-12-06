package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Resena;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ResenaRepository;
import com.gestion.hotelera.service.AuditoriaService;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resenas")
public class ResenaController {

    private final ResenaRepository resenaRepository;
    private final ReservaService reservaService;
    private final ClienteService clienteService;
    private final AuditoriaService auditoriaService;

    public ResenaController(ResenaRepository resenaRepository, ReservaService reservaService,
            ClienteService clienteService, AuditoriaService auditoriaService) {
        this.resenaRepository = resenaRepository;
        this.reservaService = reservaService;
        this.clienteService = clienteService;
        this.auditoriaService = auditoriaService;
    }

    // --- VISTA PÚBLICA / CLIENTE ---

    @GetMapping("/mis-resenas")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String misResenas(Model model, Authentication auth) {
        Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
        if (cliente == null)
            return "redirect:/login";

        List<Resena> resenas = resenaRepository.findByClienteId(cliente.getId());
        model.addAttribute("resenas", resenas);

        // Reservas finalizadas sin reseña para permitir crear nueva
        // (Lógica simplificada: listar todas las finalizadas y en la vista filtrar si
        // ya tiene reseña)
        // O mejor, listar reservas finalizadas y dejar que el cliente elija.
        // Aquí simplificamos pasando el cliente y dejando que la vista maneje enlaces.
        return "cliente-resenas";
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String crearResena(@RequestParam Long reservaId,
            @RequestParam Integer calificacion,
            @RequestParam String comentario,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
            Optional<Reserva> reservaOpt = reservaService.buscarReservaPorId(reservaId);

            if (reservaOpt.isEmpty() || !reservaOpt.get().getCliente().getId().equals(cliente.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reserva inválida.");
                return "redirect:/cliente/historial";
            }

            Reserva reserva = reservaOpt.get();
            if (!"FINALIZADA".equals(reserva.getEstadoReserva()) && !"CANCELADA".equals(reserva.getEstadoReserva())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Solo puedes reseñar estancias finalizadas o canceladas.");
                return "redirect:/cliente/historial";
            }

            // Verificar si ya existe reseña para esta reserva
            if (!resenaRepository.findByReservaId(reservaId).isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ya has enviado una reseña para esta estancia.");
                return "redirect:/cliente/historial";
            }

            Resena resena = new Resena();
            resena.setCliente(cliente);
            resena.setReserva(reserva);
            resena.setCalificacion(calificacion);
            resena.setComentario(comentario);
            resena.setAprobada(false); // Requiere moderación

            resenaRepository.save(resena);

            auditoriaService.registrarAccion("CREACION_RESENA", "Cliente envió reseña ID: " + resena.getId(), "Resena",
                    resena.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Gracias! Tu reseña ha sido enviada y está pendiente de aprobación.");
            return "redirect:/cliente/historial";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar reseña: " + e.getMessage());
            return "redirect:/cliente/historial";
        }
    }

    // --- GESTIÓN ADMIN / RECEPCIONISTA ---

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String listarResenas(Model model) {
        List<Resena> todas = resenaRepository.findAll();
        // Ordenar por fecha descendente (si tuviera fecha, usando ID como proxy o
        // añadiendo sort en repo)
        todas.sort((a, b) -> b.getId().compareTo(a.getId()));

        model.addAttribute("resenas", todas);
        return "resenas";
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String aprobarResena(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null)
            return "redirect:/resenas";
        return resenaRepository.findById(id).map(r -> {
            r.setAprobada(true);
            resenaRepository.save(r);
            auditoriaService.registrarAccion("APROBACION_RESENA", "Reseña aprobada ID: " + id, "Resena", id);
            redirectAttributes.addFlashAttribute("successMessage", "Reseña aprobada y publicada.");
            return "redirect:/resenas";
        }).orElse("redirect:/resenas");
    }

    @PostMapping("/{id}/rechazar") // O eliminar
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String eliminarResena(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null)
            return "redirect:/resenas";

        if (resenaRepository.existsById(id)) {
            resenaRepository.deleteById(id);
            auditoriaService.registrarAccion("ELIMINACION_RESENA", "Reseña eliminada/rechazada ID: " + id, "Resena",
                    id);
            redirectAttributes.addFlashAttribute("successMessage", "Reseña eliminada.");
        }
        return "redirect:/resenas";
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String responderResena(@PathVariable Long id, @RequestParam String respuesta,
            RedirectAttributes redirectAttributes) {
        if (id == null)
            return "redirect:/resenas";
        return resenaRepository.findById(id).map(r -> {
            r.setRespuesta(respuesta);
            r.setFechaRespuesta(LocalDateTime.now());
            // Al responder, asumimos que se aprueba también si no lo estaba?
            // Mejor dejar la aprobación explícita o auto-aprobar. Vamos a auto-aprobar al
            // responder.
            if (!r.getAprobada()) {
                r.setAprobada(true);
            }
            resenaRepository.save(r);
            auditoriaService.registrarAccion("RESPUESTA_RESENA", "Respuesta a reseña ID: " + id, "Resena", id);
            redirectAttributes.addFlashAttribute("successMessage", "Respuesta enviada y reseña publicada.");
            return "redirect:/resenas";
        }).orElse("redirect:/resenas");
    }
}
