package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el área personal del cliente
 * Incluye gestión de reservas del cliente
 */
@Controller
@RequestMapping("/cliente")
public class ClienteAreaController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final HabitacionService habitacionService;

    public ClienteAreaController(ClienteService clienteService, ReservaService reservaService,
            HabitacionService habitacionService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.habitacionService = habitacionService;
    }

    /**
     * Área personal del cliente - Dashboard
     */
    @GetMapping("/area")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String mostrarAreaCliente(Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Cliente cliente = clienteService.obtenerPorUsername(username);

            if (cliente == null) {
                model.addAttribute("errorMessage", "No se encontró el perfil de cliente.");
                return "error";
            }

            List<Reserva> todasReservas = reservaService.obtenerReservasPorClienteId(cliente.getId());
            long reservasActivas = todasReservas.stream()
                    .filter(r -> "ACTIVA".equals(r.getEstadoReserva()) || "PENDIENTE".equals(r.getEstadoReserva()))
                    .count();
            long reservasFinalizadas = todasReservas.stream()
                    .filter(r -> "FINALIZADA".equals(r.getEstadoReserva()))
                    .count();

            model.addAttribute("cliente", cliente);
            model.addAttribute("reservasActivas", reservasActivas);
            model.addAttribute("reservasFinalizadas", reservasFinalizadas);
            model.addAttribute("reservas", todasReservas);

            return "cliente-area";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el área del cliente: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String mostrarPerfil(Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Cliente cliente = clienteService.obtenerPorUsername(username);
            if (cliente == null) {
                return "redirect:/login";
            }
            model.addAttribute("cliente", cliente);
            return "cliente-perfil";
        } catch (Exception e) {
            return "redirect:/cliente/area";
        }
    }

    @PostMapping("/perfil/guardar")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String guardarPerfil(@ModelAttribute Cliente clienteForm, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Cliente clienteActual = clienteService.obtenerPorUsername(username);

            if (clienteActual == null) {
                return "redirect:/login";
            }

            clienteActual.setTelefono(clienteForm.getTelefono());
            clienteActual.setNacionalidad(clienteForm.getNacionalidad());

            clienteService.actualizarCliente(clienteActual);

            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
            return "redirect:/cliente/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            return "redirect:/cliente/perfil";
        }
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String mostrarHistorial(Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Cliente cliente = clienteService.obtenerPorUsername(username);

            if (cliente == null) {
                model.addAttribute("errorMessage", "Cliente no encontrado");
                return "error";
            }

            List<Reserva> reservas = reservaService.obtenerReservasPorClienteId(cliente.getId());
            reservas.sort((r1, r2) -> r2.getId().compareTo(r1.getId()));

            model.addAttribute("cliente", cliente);
            model.addAttribute("reservas", reservas);

            return "cliente-historial";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar historial: " + e.getMessage());
            return "cliente-area";
        }
    }

    // ========== GESTIÓN DE RESERVAS DEL CLIENTE ==========

    @GetMapping("/reservas/crear")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String mostrarFormularioReserva(Model model,
            Authentication auth,
            @RequestParam(name = "habitacionId", required = false) Long habitacionId) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Cliente cliente = null;
            boolean esAdminORecep = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_RECEPCIONISTA"));

            if (!esAdminORecep) {
                cliente = clienteService.obtenerPorUsername(auth.getName());
                if (cliente == null) {
                    model.addAttribute("errorMessage", "No se encontró el perfil de cliente asociado a su usuario.");
                    return "error";
                }
                model.addAttribute("cliente", cliente);
            }

            model.addAttribute("reserva", new Reserva());
            // Si es admin, mostrar todas las habitaciones disponibles (o filtrar por
            // cliente si se seleccionara antes, pero aquí es general)
            // Para simplificar, usamos un ID de cliente ficticio o null si el servicio lo
            // soporta, o el del usuario actual
            Long clienteId = (cliente != null) ? cliente.getId() : 0L;

            model.addAttribute("habitacionesDisponibles",
                    habitacionService.obtenerHabitacionesDisponiblesParaCliente(clienteId));

            if (habitacionId != null && habitacionId > 0) {
                model.addAttribute("habitacionId", habitacionId);
            }
            return "generarReserva";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el formulario: " + e.getMessage());
            return "generarReserva";
        }
    }

    @PostMapping("/reservas/guardar")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public String guardarReserva(@ModelAttribute Reserva reserva,
            @RequestParam("habitacionId") Long habitacionId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (habitacionId == null || habitacionId <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Habitación inválida");
                return "redirect:/cliente/reservas/crear";
            }

            Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
            if (cliente == null) {
                return "redirect:/login";
            }

            Optional<Habitacion> habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
            if (habitacionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Habitación no encontrada");
                return "redirect:/cliente/reservas/crear";
            }

            reserva.setCliente(cliente);
            reserva.setHabitacion(habitacionOpt.get());

            if (reserva.getFechaInicio().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La fecha de inicio no puede ser anterior a hoy.");
                return "redirect:/cliente/reservas/crear";
            }

            Integer dias = reservaService.calcularDiasEstadia(reserva.getFechaInicio(), reserva.getFechaFin());
            reserva.setDiasEstadia(dias);
            reserva.setTotalPagar(reservaService.calcularTotalPagar(habitacionOpt.get().getPrecioPorNoche(), dias));
            reserva.setHoraEntrada(java.time.LocalTime.of(14, 0));
            reserva.setHoraSalida(java.time.LocalTime.of(12, 0));

            reserva.setEstadoReserva("PROCESANDO");

            Reserva reservaGuardada = reservaService.crearOActualizarReserva(reserva);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Reserva creada exitosamente. Ahora puedes añadir servicios opcionales.");
            return "redirect:/reservas/" + reservaGuardada.getId() + "/servicios?returnTo=dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la reserva: " + e.getMessage());
            return "redirect:/cliente/reservas/crear";
        }
    }

    @PostMapping("/reservas/solicitar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String solicitarReserva(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam String tipo,
            @RequestParam(required = false) String clienteDni,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication() == null) {
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            }

            Cliente cliente = null;
            boolean esAdminORecep = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_RECEPCIONISTA"));

            if (esAdminORecep && clienteDni != null && !clienteDni.trim().isEmpty()) {
                Optional<Cliente> clienteOpt = clienteService.buscarClientePorDni(clienteDni.trim());
                if (clienteOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "No se encontró un cliente con el DNI: " + clienteDni);
                    return "redirect:/cliente/reservas/crear";
                }
                cliente = clienteOpt.get();
            } else {
                cliente = clienteService.obtenerPorUsername(auth.getName());
                if (cliente == null) {
                    return "redirect:/login";
                }
            }

            if (fechaEntrada.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "La fecha de entrada no puede ser anterior a hoy.");
                return "redirect:/cliente/reservas/crear";
            }

            if (fechaSalida.isBefore(fechaEntrada.plusDays(1))) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "La fecha de salida debe ser posterior a la entrada.");
                return "redirect:/cliente/reservas/crear";
            }

            var disponibles = habitacionService.obtenerHabitacionesDisponiblesParaCliente(cliente.getId());

            var habitacionOpt = disponibles.stream()
                    .filter(h -> h.getTipo().equalsIgnoreCase(tipo))
                    .findFirst();

            if (habitacionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No hay habitaciones disponibles del tipo " + tipo + " para estas fechas.");
                return "redirect:/cliente/reservas/crear";
            }

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setHabitacion(habitacionOpt.get());
            reserva.setFechaInicio(fechaEntrada);
            reserva.setFechaFin(fechaSalida);

            Integer dias = reservaService.calcularDiasEstadia(fechaEntrada, fechaSalida);
            reserva.setDiasEstadia(dias);
            reserva.setTotalPagar(reservaService.calcularTotalPagar(habitacionOpt.get().getPrecioPorNoche(), dias));
            reserva.setHoraEntrada(java.time.LocalTime.of(14, 0));
            reserva.setHoraSalida(java.time.LocalTime.of(12, 0));

            reserva.setEstadoReserva("PROCESANDO");

            Reserva reservaGuardada = reservaService.crearOActualizarReserva(reserva);

            if (esAdminORecep) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Reserva creada exitosamente para el cliente. El cliente debe completar el pago.");
                return "redirect:/reservas";
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Habitación asignada exitosamente. Personaliza tu estancia.");
                return "redirect:/reservas/" + reservaGuardada.getId() + "/servicios?returnTo=dashboard";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/cliente/reservas/crear";
        }
    }

    @GetMapping("/reservas/calcular-costo")
    @ResponseBody
    public String calcularCosto(
            @RequestParam("habitacionId") Long habitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Optional<Habitacion> habitacionOptional = habitacionService.buscarHabitacionPorId(habitacionId);
        if (habitacionOptional.isEmpty()) {
            return "{\"error\": \"Habitación no encontrada\"}";
        }

        Habitacion habitacion = habitacionOptional.get();
        Integer dias = reservaService.calcularDiasEstadia(fechaInicio, fechaFin);
        Double total = reservaService.calcularTotalPagar(habitacion.getPrecioPorNoche(), dias);
        return String.format("{\"dias\": %d, \"total\": %.2f}", dias, total);
    }
}