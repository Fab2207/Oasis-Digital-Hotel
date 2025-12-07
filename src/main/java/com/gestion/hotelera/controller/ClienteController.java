package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/clientes")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listarClientes(
            Model model,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<Cliente> clientesPage = clienteService.obtenerClientesPaginados(pageRequest, search);

        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("currentPage", clientesPage.getNumber());
        model.addAttribute("pageSize", clientesPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        return "clientes";
    }

    // Mantener /historial por compatibilidad o redirigir
    @GetMapping("/historial")
    public String mostrarHistorialRedir(Model model) {
        return "redirect:/clientes";
    }

    /**
     * Formulario de registro de cliente - Solo ADMIN y RECEPCIONISTA
     */
    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        Cliente cliente = new Cliente();
        cliente.setUsuario(new com.gestion.hotelera.model.Usuario());
        model.addAttribute("cliente", cliente);
        return "registroCliente";
    }

    /**
     * Guardar nuevo cliente - Solo ADMIN y RECEPCIONISTA pueden registrar
     */
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("cliente") Cliente cliente,
            RedirectAttributes redirectAttributes) {
        try {
            Cliente guardado = clienteService.crearCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente registrado correctamente: "
                    + guardado.getNombres() + " " + guardado.getApellidos());
            // Redirigir a flujo de reserva para el cliente recién creado
            Long clienteId = guardado.getId();
            return "redirect:/reservas/crear?idCliente=" + (clienteId != null ? clienteId.toString() : "");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al registrar el cliente.");
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        }
    }

    /**
     * Ver detalles del cliente (Solo lectura)
     */
    @GetMapping("/ver/{id}")
    public String verCliente(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            return clienteService.obtenerClientePorId(id)
                    .map(cliente -> {
                        model.addAttribute("cliente", cliente);
                        model.addAttribute("readonly", true);
                        return "cliente-ver";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
                        return "redirect:/clientes";
                    });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar el cliente");
            return "redirect:/clientes";
        }
    }

    /**
     * Formulario de edición de cliente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return clienteService.obtenerClientePorId(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    return "editar-cliente";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
                    return "redirect:/clientes";
                });
    }

    /**
     * Actualizar cliente existente
     */
    @PostMapping("/actualizar")
    public String actualizarCliente(@ModelAttribute("cliente") Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            clienteService.actualizarCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente actualizado correctamente");
            return "redirect:/clientes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar cliente: " + e.getMessage());
            return "redirect:/clientes/editar/" + cliente.getId();
        }
    }

    /**
     * API para buscar cliente por DNI (AJAX)
     */
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarClienteApi(@RequestParam String dni) {
        return clienteService.buscarClientePorDni(dni)
                .map(cliente -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", cliente.getId());
                    response.put("nombres", cliente.getNombres());
                    response.put("apellidos", cliente.getApellidos());
                    response.put("dni", cliente.getDni());
                    response.put("email", cliente.getEmail());
                    response.put("telefono", cliente.getTelefono());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar cliente
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de cliente no válido");
            return "redirect:/clientes";
        }
        try {
            if (clienteService.eliminarClientePorId(id)) {
                redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el cliente");
            }
        } catch (com.gestion.hotelera.exception.ClienteConReservasActivasException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se puede eliminar: El cliente tiene reservas activas. Cancele o finalice las reservas primero.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar cliente");
        }
        return "redirect:/clientes";
    }

}