package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.service.ServicioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // ADMIN y RECEPCIONISTA pueden ver servicios
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE')")
    @GetMapping
    public String listarServicios(Model model, Authentication auth) {
        model.addAttribute("servicios", servicioService.obtenerTodosLosServicios());

        // Verificar si es ADMIN para permitir edición
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "servicios";
    }

    // Solo ADMIN puede acceder al formulario de creación
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("servicio", new Servicio());
        return "registrarServicio";
    }

    // Solo ADMIN puede acceder al formulario de edición
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return servicioService.obtenerServicioPorId(id)
                .map(servicio -> {
                    model.addAttribute("servicio", servicio);
                    return "editarServicio";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Servicio no encontrado");
                    return "redirect:/servicios";
                });
    }

    // Solo ADMIN puede crear/actualizar servicios
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/guardar")
    public String guardarServicio(@ModelAttribute Servicio servicio, RedirectAttributes redirectAttributes) {
        try {
            if (servicio.getId() == null) {
                servicioService.crearServicio(servicio);
                redirectAttributes.addFlashAttribute("successMessage", "Servicio creado exitosamente");
            } else {
                servicioService.actualizarServicio(servicio);
                redirectAttributes.addFlashAttribute("successMessage", "Servicio actualizado exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar servicio: " + e.getMessage());
        }
        return "redirect:/servicios";
    }

    // Solo ADMIN puede eliminar servicios
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/eliminar")
    public String eliminarServicio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioService.eliminarServicio(id);
            redirectAttributes.addFlashAttribute("successMessage", "Servicio eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar servicio: " + e.getMessage());
        }
        return "redirect:/servicios";
    }
}
