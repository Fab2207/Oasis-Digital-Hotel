package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.NotificacionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;
    private final NotificacionService notificacionService;

    public HabitacionController(HabitacionService habitacionService, NotificacionService notificacionService) {
        this.habitacionService = habitacionService;
        this.notificacionService = notificacionService;
    }

    // ADMIN y RECEPCIONISTA pueden ver habitaciones
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @GetMapping
    public String listarHabitaciones(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "numero") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Authentication auth) {
        try {
            org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                    : org.springframework.data.domain.Sort.by(sortBy).descending();

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                    size, sort);

            // Actualizar estados antes de listar para reflejar disponibilidad REAL de hoy
            habitacionService.actualizarEstadosHabitacionesHoy();

            org.springframework.data.domain.Page<Habitacion> habitacionesPage = habitacionService
                    .obtenerHabitacionesPaginadas(pageable, search);

            model.addAttribute("habitaciones", habitacionesPage.getContent());
            model.addAttribute("habitacionesPage", habitacionesPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", habitacionesPage.getTotalPages());
            model.addAttribute("totalItems", habitacionesPage.getTotalElements());
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);

            // Verificar si es ADMIN para permitir edición
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);

            model.addAttribute("habitacion", new Habitacion()); // Para el modal de creación
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar habitaciones");
        }
        return "habitaciones";
    }

    // Vista pública para clientes: solo tipo y precio, estilo tarjetas
    @GetMapping("/publico")
    public String listarHabitacionesPublico(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        return "habitaciones_publico";
    }

    // Solo ADMIN puede crear/editar habitaciones
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/guardar")
    public String guardarHabitacion(@ModelAttribute Habitacion habitacion, RedirectAttributes redirectAttributes) {
        try {
            if (habitacion.getId() == null || habitacion.getId() == 0) {
                habitacionService.crearHabitacion(habitacion);
                notificacionService.crearNotificacion("Nueva Habitación",
                        "Se ha creado la habitación " + habitacion.getNumero(), "Sistema");
                redirectAttributes.addFlashAttribute("successMessage", "Habitación creada exitosamente.");
            } else {
                habitacionService.actualizarHabitacion(habitacion);
                notificacionService.crearNotificacion("Habitación Actualizada",
                        "Se ha actualizado la habitación " + habitacion.getNumero(), "Sistema");
                redirectAttributes.addFlashAttribute("successMessage", "Habitación actualizada exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/habitaciones";
    }

    // Solo ADMIN puede eliminar habitaciones
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/eliminar/{id}")
    public String eliminarHabitacion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "ID de habitación inválido");
                return "redirect:/habitaciones";
            }
            habitacionService.eliminarHabitacion(id);
            notificacionService.crearNotificacion("Habitación Eliminada", "Se ha eliminado la habitación con ID " + id,
                    "Sistema");
            redirectAttributes.addFlashAttribute("successMessage", "Habitación eliminada exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la habitación");
        }
        return "redirect:/habitaciones";
    }

    // Endpoint API para obtener habitaciones disponibles por rango de fechas (AJAX)
    @GetMapping("/api/disponibles")
    @ResponseBody
    public java.util.List<Habitacion> obtenerHabitacionesDisponibles(
            @RequestParam("inicio") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate inicio,
            @RequestParam("fin") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fin) {
        return habitacionService.buscarDisponibles(inicio, fin);
    }
}