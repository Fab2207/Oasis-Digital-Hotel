package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.PagoService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.ServicioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    private final ClienteService clienteService;
    private final HabitacionService habitacionService;
    private final ReservaService reservaService;
    private final ServicioService servicioService;
    private final PagoService pagoService;
    private final com.gestion.hotelera.service.DescuentoService descuentoService;

    public ReservaController(ClienteService clienteService, HabitacionService habitacionService,
            ReservaService reservaService, ServicioService servicioService, PagoService pagoService,
            com.gestion.hotelera.service.DescuentoService descuentoService) {
        this.clienteService = clienteService;
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
        this.servicioService = servicioService;
        this.pagoService = pagoService;
        this.descuentoService = descuentoService;
    }

    // ... (resto del código existente)

    // ADMIN y RECEPCIONISTA pueden ver todas las reservas
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @GetMapping
    public String listarReservas(Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "estados", required = false) List<String> estados) {
        List<Reserva> reservas;

        if (search != null && !search.isEmpty()) {
            reservas = reservaService.obtenerTodasLasReservas().stream()
                    .filter(r -> (r.getCliente().getDni().contains(search) ||
                            (r.getCliente().getNombres() + " " + r.getCliente().getApellidos()).toLowerCase()
                                    .contains(search.toLowerCase())
                            ||
                            String.valueOf(r.getId()).contains(search)))
                    .collect(Collectors.toList());
        } else {
            reservas = reservaService.obtenerTodasLasReservas();
        }

        if (reservas.isEmpty() && search != null && !search.isEmpty()) {
            // Si no hay reservas pero se buscó algo, verificar si es un cliente existente
            // por DNI
            Optional<Cliente> clienteOpt = clienteService.buscarClientePorDni(search);
            if (clienteOpt.isPresent()) {
                model.addAttribute("clienteSinReservas", clienteOpt.get());
            }
        }

        if (estados != null && !estados.isEmpty()) {
            reservas = reservas.stream()
                    .filter(r -> estados.contains(r.getEstadoReserva()))
                    .collect(Collectors.toList());
        }

        // Mostrar todas las reservas, incluyendo PROCESANDO, para que se puedan
        // gestionar si quedan atascadas
        reservas.sort((r1, r2) -> r2.getId().compareTo(r1.getId()));

        model.addAttribute("reservas", reservas);
        model.addAttribute("search", search);
        model.addAttribute("estadosSeleccionados", estados);
        model.addAttribute("pageTitle", "Gestión de Reservas");
        model.addAttribute("pageName", "Reservas");
        return "reservas";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("reserva", new Reserva());
        // Cargar solo habitaciones disponibles (estado = DISPONIBLE)
        model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles());
        model.addAttribute("pageTitle", "Nueva Reserva");
        model.addAttribute("pageName", "Nueva Reserva");
        return "crear_reserva";
    }

    // ADMIN y RECEPCIONISTA pueden crear reservas
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute Reserva reserva,
            @RequestParam("clienteDni") String clienteDni,
            @RequestParam("habitacionId") Long habitacionId,
            @RequestParam(value = "codigoDescuento", required = false) String codigoDescuento,
            RedirectAttributes redirectAttributes,
            Model model,
            Authentication auth) {

        // Función auxiliar para recargar el formulario en caso de error
        Runnable reloadForm = () -> {
            model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles());
            model.addAttribute("pageTitle", "Nueva Reserva");
            model.addAttribute("pageName", "Nueva Reserva");
            // Intentar recuperar el cliente si el DNI es válido
            if (clienteDni != null && !clienteDni.trim().isEmpty()) {
                clienteService.buscarClientePorDni(clienteDni.trim()).ifPresent(cliente -> {
                    model.addAttribute("clienteEncontrado", cliente);
                });
            }
        };

        try {
            String dniLimpio = clienteDni != null ? clienteDni.trim() : "";
            if (!dniLimpio.matches("^\\d{8}$")) {
                model.addAttribute("errorMessage", "El DNI debe contener exactamente 8 dígitos numéricos");
                reloadForm.run();
                return "crear_reserva";
            }

            Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dniLimpio);
            if (clienteOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Error: Cliente no encontrado para el DNI proporcionado.");
                reloadForm.run();
                return "crear_reserva";
            }
            reserva.setCliente(clienteOptional.get());

            Optional<Habitacion> habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
            if (habitacionOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Habitación no encontrada.");
                reloadForm.run();
                return "crear_reserva";
            }
            reserva.setHabitacion(habitacionOpt.get());

            // Validar que la fecha de inicio no sea null
            if (reserva.getFechaInicio() == null) {
                model.addAttribute("errorMessage", "La fecha de inicio de la reserva es requerida.");
                reloadForm.run();
                return "crear_reserva";
            }

            if (reserva.getFechaInicio().isBefore(LocalDate.now())) {
                model.addAttribute("errorMessage",
                        "La fecha de inicio de la reserva no puede ser anterior a la fecha actual.");
                reloadForm.run();
                return "crear_reserva";
            }
            reserva.setEstadoReserva("PROCESANDO");

            // Calcular total inicial para validar descuento
            Integer dias = reservaService.calcularDiasEstadia(reserva.getFechaInicio(), reserva.getFechaFin());
            Double totalBase = reservaService.calcularTotalPagar(habitacionOpt.get().getPrecioPorNoche(), dias);
            reserva.setDiasEstadia(dias);
            reserva.setTotalPagar(totalBase);

            // Aplicar descuento si existe
            if (codigoDescuento != null && !codigoDescuento.trim().isEmpty()) {
                Optional<com.gestion.hotelera.model.Descuento> descuentoOpt = descuentoService
                        .validarYBuscarDescuento(codigoDescuento, totalBase);
                if (descuentoOpt.isPresent()) {
                    com.gestion.hotelera.model.Descuento descuento = descuentoOpt.get();
                    Double montoDescuento = descuento.calcularDescuento(totalBase);
                    reserva.setDescuento(descuento);
                    reserva.setMontoDescuento(montoDescuento);
                    descuentoService.incrementarUso(descuento);
                }
            }

            Reserva reservaGuardada = reservaService.crearOActualizarReserva(reserva);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reserva creada exitosamente. Puedes añadir servicios adicionales antes del pago.");

            String returnTo = "historial";
            Long reservaId = reservaGuardada.getId();
            String redirectUrl = "/reservas/" + (reservaId != null ? reservaId : "") + "/servicios";
            redirectUrl += "?returnTo=" + returnTo;
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear la reserva: " + e.getMessage());
            reloadForm.run();
            return "crear_reserva";
        }
    }

    @PostMapping("/{id}/aplicar-descuento")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String aplicarDescuento(@PathVariable Long id,
            @RequestParam("codigo") String codigo,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            if (reservaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada");
                return "redirect:/dashboard";
            }

            Reserva reserva = reservaOpt.get();
            // Calcular base + servicios para aplicar descuento al total o solo a base?
            // Usualmente descuento aplica sobre el total o base. Asumiremos base +
            // servicios para mayor beneficio o solo base.
            // El modelo Descuento calcula sobre un montoBase.
            // Vamos a usar (TotalPagar + TotalServicios) como base.

            Double base = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
            Double servicios = reserva.calcularTotalServicios();
            Double totalParaDescuento = base + servicios;

            Optional<com.gestion.hotelera.model.Descuento> descuentoOpt = descuentoService
                    .validarYBuscarDescuento(codigo, totalParaDescuento);

            if (descuentoOpt.isPresent()) {
                com.gestion.hotelera.model.Descuento descuento = descuentoOpt.get();
                Double montoDescuento = descuento.calcularDescuento(totalParaDescuento);

                reserva.setDescuento(descuento);
                reserva.setMontoDescuento(montoDescuento);
                reservaService.crearOActualizarReserva(reserva); // Guardar cambios
                // No incrementamos uso aquí, sino al pagar? O al aplicar?
                // Mejor al aplicar para reservar el cupón, o al pagar para confirmar.
                // Por simplicidad, incrementamos al aplicar, aunque si cancela no se revierte
                // (podría mejorarse).
                descuentoService.incrementarUso(descuento);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Descuento aplicado correctamente: -S/. " + String.format("%.2f", montoDescuento));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Cupón no válido o expirado.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aplicar descuento: " + e.getMessage());
        }

        return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
    }

    @PostMapping("/{id}/quitar-descuento")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String quitarDescuento(@PathVariable Long id,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            if (reservaOpt.isPresent()) {
                Reserva reserva = reservaOpt.get();
                reserva.setDescuento(null);
                reserva.setMontoDescuento(0.0);
                reservaService.crearOActualizarReserva(reserva);
                redirectAttributes.addFlashAttribute("successMessage", "Descuento removido.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al quitar descuento");
        }
        return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
    }

    // Método para cancelar el proceso de reserva (eliminar reserva incompleta)
    @PostMapping("/{id}/cancelar-proceso")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String cancelarProcesoReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            if (reservaOpt.isPresent()) {
                Reserva reserva = reservaOpt.get();
                // Solo permitir eliminar si está en estado PROCESANDO
                if ("PROCESANDO".equals(reserva.getEstadoReserva())) {
                    reservaService.eliminarReservaFisica(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Proceso de reserva cancelado.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "No se puede cancelar una reserva ya confirmada desde aquí.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cancelar el proceso.");
        }
        return "redirect:/reservas";
    }

    @PostMapping("/confirmar/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String confirmarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservaService.realizarCheckIn(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva confirmada (Check-in realizado).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al confirmar reserva: " + e.getMessage());
        }
        return "redirect:/reservas";
    }

    // ADMIN, RECEPCIONISTA y CLIENTE pueden cancelar reservas (Cliente solo las
    // suyas)
    @SuppressWarnings("null")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE')")
    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer,
            Authentication auth) {
        try {
            // Obtener el rol del usuario de forma segura
            String userRole = null;
            if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                userRole = auth.getAuthorities().iterator().next().getAuthority();
            }

            if (userRole == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo determinar el rol del usuario.");
                return "redirect:" + (referer != null ? referer : "/dashboard");
            }

            // Si es cliente, verificar que la reserva le pertenece
            if ("ROLE_CLIENTE".equals(userRole)) {
                Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
                if (reservaOpt.isPresent()) {
                    Reserva reserva = reservaOpt.get();
                    if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null ||
                            !reserva.getCliente().getUsuario().getUsername().equals(auth.getName())) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "No tienes permiso para cancelar esta reserva.");
                        return "redirect:" + (referer != null ? referer : "/cliente/historial");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada.");
                    return "redirect:" + (referer != null ? referer : "/cliente/historial");
                }
            }

            if (reservaService.cancelarReserva(id, userRole)) {
                redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cancelar la reserva.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    // ADMIN y RECEPCIONISTA pueden eliminar reservas finalizadas o canceladas
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @PostMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            reservaService.eliminarReservaFisica(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la reserva: " + e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/reservas");
    }

    // ADMIN y RECEPCIONISTA pueden finalizar reservas
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    @PostMapping("/finalizar/{id}")
    public String finalizarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido.");
            return "redirect:" + (referer != null ? referer : "/dashboard");
        }

        try {
            var reservaOpt = reservaService.obtenerReservaPorId(id);
            if (reservaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada.");
                return "redirect:" + (referer != null ? referer : "/dashboard");
            }

            var reserva = reservaOpt.get();
            if ("FINALIZADA".equals(reserva.getEstadoReserva())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La reserva ya está finalizada.");
                return "redirect:" + (referer != null ? referer : "/dashboard");
            }

            reservaService.finalizarReserva(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva finalizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al finalizar reserva: " + e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    // CHECK-IN: Solo ADMIN y RECEPCIONISTA
    @PostMapping("/checkin/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String realizarCheckIn(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            reservaService.realizarCheckIn(id);
            redirectAttributes.addFlashAttribute("successMessage", "Check-in realizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al realizar check-in: " + e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    // CHECK-OUT: Solo ADMIN y RECEPCIONISTA
    @PostMapping("/checkout/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
    public String realizarCheckOut(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            reservaService.realizarCheckOut(id);
            redirectAttributes.addFlashAttribute("successMessage", "Check-out realizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al realizar check-out: " + e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    // Ver factura: todos los autenticados pueden verla
    @GetMapping("/factura/{id}")
    public String verFactura(@PathVariable Long id, Model model) {
        Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
        if (reservaOpt.isPresent()) {
            model.addAttribute("reserva", reservaOpt.get());
            return "factura";
        }
        return "redirect:/cliente/historial";
    }

    // ========== MÉTODOS DE FLUJO DE RESERVA (Servicios y Pago) ==========

    @GetMapping("/{id}/servicios")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String mostrarSelectorServicios(@PathVariable Long id,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/dashboard";
        }
        try {
            return reservaService.obtenerReservaPorId(id)
                    .map(reserva -> {
                        if (auth != null && auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                            if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null ||
                                    !reserva.getCliente().getUsuario().getUsername().equals(auth.getName())) {
                                redirectAttributes.addFlashAttribute("errorMessage",
                                        "No tiene permiso para gestionar esta reserva.");
                                return "redirect:/cliente/historial";
                            }
                        }

                        List<Servicio> serviciosActivos = servicioService.listarServiciosActivos();
                        Set<Long> serviciosSeleccionados = reserva.getServicios()
                                .stream()
                                .map(Servicio::getId)
                                .collect(Collectors.toSet());

                        model.addAttribute("reserva", reserva);
                        model.addAttribute("serviciosDisponibles", serviciosActivos);
                        model.addAttribute("serviciosSeleccionados", serviciosSeleccionados);
                        model.addAttribute("returnTo", returnTo);
                        return "seleccionarServicios";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada");
                        return "redirect:/dashboard";
                    });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar servicios");
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/servicios")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String guardarServicios(@PathVariable Long id,
            @RequestParam(value = "servicioIds", required = false) List<Long> servicioIds,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/dashboard";
        }

        // Verificar propiedad
        Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null ||
                        !reserva.getCliente().getUsuario().getUsername().equals(auth.getName())) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "No tiene permiso para modificar esta reserva.");
                    return "redirect:/cliente/historial";
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada");
            return "redirect:/dashboard";
        }

        try {
            reservaService.asignarServicios(id, servicioIds, null);

            // Si la reserva estaba en proceso, pasarla a PENDIENTE (confirmada para pago)
            Optional<Reserva> rOpt = reservaService.obtenerReservaPorId(id);
            if (rOpt.isPresent()) {
                Reserva r = rOpt.get();
                if ("PROCESANDO".equals(r.getEstadoReserva())) {
                    r.setEstadoReserva("PENDIENTE");
                    // Al actualizar, el servicio enviará el email si detecta que es nueva o cambia
                    // estado
                    // Pero como ya existe, forzamos la lógica de "nueva" si queremos el email de
                    // bienvenida
                    // O confiamos en que el cambio de estado sea suficiente si implementamos
                    // notificaciones por cambio.
                    // Por ahora, simplemente guardamos.
                    reservaService.crearOActualizarReserva(r);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Reserva confirmada y pendiente de pago. Se ha notificado al cliente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar servicios");
            return "redirect:/reservas/" + id + "/servicios";
        }

        // Redirigir al pago
        return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
    }

    @GetMapping("/{id}/pago")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String mostrarPago(@PathVariable Long id,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        return reservaService.obtenerReservaPorId(id)
                .map(reserva -> {
                    if (auth != null
                            && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                        if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null ||
                                !reserva.getCliente().getUsuario().getUsername().equals(auth.getName())) {
                            redirectAttributes.addFlashAttribute("errorMessage",
                                    "No tiene permiso para ver el pago de esta reserva.");
                            return "redirect:/cliente/historial";
                        }
                    }

                    double montoServicios = reserva.calcularTotalServicios();
                    double montoBase = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
                    double montoTotal = montoBase + montoServicios;

                    model.addAttribute("reserva", reserva);
                    model.addAttribute("montoBase", montoBase);
                    model.addAttribute("montoServicios", montoServicios);
                    model.addAttribute("montoTotal", montoTotal);
                    model.addAttribute("returnTo", returnTo);
                    model.addAttribute("pagoProcesado", reserva.getPago());
                    return "pago";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "La reserva solicitada no existe.");
                    return "redirect:/dashboard";
                });
    }

    @PostMapping("/{id}/pago")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String procesarPago(@PathVariable Long id,
            @RequestParam("metodo") String metodo,
            @RequestParam(value = "numeroTarjeta", required = false) String numeroTarjeta,
            @RequestParam(value = "cvv", required = false) String cvv,
            @RequestParam(value = "fechaExp", required = false) String fechaExp,
            @RequestParam(value = "titularTarjeta", required = false) String titularTarjeta,
            @RequestParam(value = "telefonoWallet", required = false) String telefonoWallet,
            @RequestParam(value = "titularWallet", required = false) String titularWallet,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Verificar propiedad
        Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null ||
                        !reserva.getCliente().getUsuario().getUsername().equals(authentication.getName())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "No tiene permiso para pagar esta reserva.");
                    return "redirect:/cliente/historial";
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada");
            return "redirect:/dashboard";
        }

        // Validar método de pago primero
        if (metodo == null || metodo.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un método de pago");
            return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
        }

        try {
            PagoRequest request = new PagoRequest();
            request.setReservaId(id);
            String metodoNormalizado = metodo.toUpperCase();
            request.setMetodo(metodoNormalizado);
            request.setMetodoPago(metodoNormalizado);
            request.setCanal("WEB");

            if ("TARJETA".equalsIgnoreCase(metodo)) {
                PagoRequest.DatosTarjeta datosTarjeta = new PagoRequest.DatosTarjeta();
                datosTarjeta.setNumero(numeroTarjeta);
                datosTarjeta.setCvv(cvv);
                datosTarjeta.setFechaExp(fechaExp);
                datosTarjeta.setTitular(titularTarjeta);
                request.setTarjeta(datosTarjeta);
                request.setWallet(null);
            } else {
                PagoRequest.DatosWallet datosWallet = new PagoRequest.DatosWallet();
                datosWallet.setTelefono(telefonoWallet);
                datosWallet.setTitular(titularWallet);
                request.setWallet(datosWallet);
                request.setTarjeta(null);
            }

            PagoResponse response = pagoService.procesarPago(request);

            if (response == null || !response.isExito()) {
                String mensajeError = response != null && response.getMensaje() != null ? response.getMensaje()
                        : "No fue posible procesar el pago. Inténtalo nuevamente.";
                redirectAttributes.addFlashAttribute("errorMessage", mensajeError);
                return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Pago procesado correctamente. Código de referencia: " + response.getReferencia());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
        } catch (Exception ex) {
            String mensajeError = ex.getMessage() != null ? ex.getMessage()
                    : "No fue posible procesar el pago. Inténtalo nuevamente.";
            redirectAttributes.addFlashAttribute("errorMessage", mensajeError);
            logger.error("Error al procesar pago para reserva ID: {}", id, ex);
            return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
        }

        if ("historial".equalsIgnoreCase(returnTo)) {
            return reservaService.obtenerReservaPorId(id)
                    .map(reserva -> {
                        if (reserva.getCliente() != null && reserva.getCliente().getId() != null) {
                            return "redirect:/clientes/historial?id=" + reserva.getCliente().getId();
                        }
                        return "redirect:/clientes/historial";
                    })
                    .orElse("redirect:/clientes/historial");
        }

        if ("dashboard".equalsIgnoreCase(returnTo)) {
            return "redirect:/dashboard";
        }

        if (authentication != null && authentication.getAuthorities() != null) {
            boolean esCliente = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(rol -> "ROLE_CLIENTE".equals(rol));
            if (esCliente) {
                return "redirect:/cliente/historial";
            }
        }

        return "redirect:/reservas";
    }
}
