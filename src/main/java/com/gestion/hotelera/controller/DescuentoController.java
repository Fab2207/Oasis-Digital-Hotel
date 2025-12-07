package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Descuento;
import com.gestion.hotelera.service.DescuentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/descuentos")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE')")
public class DescuentoController {

    private final DescuentoService descuentoService;

    public DescuentoController(DescuentoService descuentoService) {
        this.descuentoService = descuentoService;
    }

    @GetMapping
    public String listarDescuentos(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                : org.springframework.data.domain.Sort.by(sortBy).descending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);

        org.springframework.data.domain.Page<Descuento> descuentosPage = descuentoService
                .obtenerDescuentosPaginados(pageable, search);

        model.addAttribute("descuentos", descuentosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", descuentosPage.getTotalPages());
        model.addAttribute("totalItems", descuentosPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "descuentos";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("descuento", new Descuento());
        return "registrarDescuento";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Descuento descuento = descuentoService.obtenerPorId(id)
                    .orElseThrow(() -> new Exception("Descuento no encontrado"));
            model.addAttribute("descuento", descuento);
            return "editarDescuento";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar el descuento: " + e.getMessage());
            return "redirect:/descuentos";
        }
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String crearDescuento(@ModelAttribute Descuento descuento, RedirectAttributes redirectAttributes) {
        try {
            descuentoService.crearDescuento(descuento);
            redirectAttributes.addFlashAttribute("successMessage", "Descuento creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear descuento: " + e.getMessage());
        }
        return "redirect:/descuentos";
    }

    @PostMapping("/{id}/actualizar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String actualizarDescuento(@PathVariable Long id, @ModelAttribute Descuento descuento,
            RedirectAttributes redirectAttributes) {
        try {
            descuento.setId(id);
            descuentoService.actualizarDescuento(descuento);
            redirectAttributes.addFlashAttribute("successMessage", "Descuento actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar descuento: " + e.getMessage());
        }
        return "redirect:/descuentos";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String eliminarDescuento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            descuentoService.eliminarDescuento(id);
            redirectAttributes.addFlashAttribute("successMessage", "Descuento eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar descuento: " + e.getMessage());
        }
        return "redirect:/descuentos";
    }
}
