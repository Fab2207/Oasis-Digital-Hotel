package com.gestion.hotelera.controller;

import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private com.gestion.hotelera.service.NotificacionService notificacionService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            var roles = auth.getAuthorities();
            model.addAttribute("roles", roles);

            // Logic for CLIENTE
            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                try {
                    var cliente = clienteService.obtenerPorUsername(auth.getName());
                    if (cliente != null) {
                        var reservas = reservaService.obtenerReservasPorClienteId(cliente.getId());

                        long reservasActivas = reservas.stream()
                                .filter(r -> "ACTIVA".equals(r.getEstadoReserva())
                                        || "PENDIENTE".equals(r.getEstadoReserva()))
                                .count();
                        long reservasFinalizadas = reservas.stream()
                                .filter(r -> "FINALIZADA".equals(r.getEstadoReserva()))
                                .count();

                        model.addAttribute("cliente", cliente);
                        model.addAttribute("reservasActivas", reservasActivas);
                        model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                        model.addAttribute("reservas", reservas);
                    }
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "Error al cargar datos del cliente");
                }
                return "redirect:/cliente/area";
            }

            // Logic for ADMIN and RECEPCIONISTA
            if (roles.stream().anyMatch(
                    r -> r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_RECEPCIONISTA"))) {

                // Información del usuario actual (sin EmpleadoService)
                model.addAttribute("usuarioActual", auth.getName());

                long totalHabitaciones = habitacionService.contarHabitaciones();
                long totalClientes = clienteService.contarClientes();
                long totalReservas = reservaService.contarReservas();
                long habitacionesDisponibles = habitacionService.contarDisponibles();
                long habitacionesOcupadas = habitacionService.contarOcupadas();
                long habitacionesMantenimiento = habitacionService.contarEnMantenimiento();
                // totalEmpleados removido ya que EmpleadoService no existe
                double ingresosTotales = reservaService.calcularIngresosTotales();
                long reservasPendientes = reservaService.contarReservasPorEstado("PENDIENTE");
                long reservasActivas = reservaService.contarReservasPorEstado("ACTIVA");
                long checkInsHoy = reservaService.contarCheckInsHoy();
                long checkOutsHoy = reservaService.contarCheckOutsHoy();
                var ingresosUltimos30Dias = reservaService.getIngresosUltimosDias(30);

                model.addAttribute("totalHabitaciones", totalHabitaciones);
                model.addAttribute("totalClientes", totalClientes);
                model.addAttribute("totalReservas", totalReservas);
                model.addAttribute("habitacionesDisponibles", habitacionesDisponibles);
                model.addAttribute("habitacionesOcupadas", habitacionesOcupadas);
                model.addAttribute("habitacionesMantenimiento", habitacionesMantenimiento);
                model.addAttribute("ingresosTotales", ingresosTotales);
                model.addAttribute("reservasPendientes", reservasPendientes);
                model.addAttribute("reservasActivas", reservasActivas);
                model.addAttribute("ingresosUltimos30Dias", ingresosUltimos30Dias);
                model.addAttribute("checkInsHoy", checkInsHoy);
                model.addAttribute("checkOutsHoy", checkOutsHoy);
                model.addAttribute("checkInsHoy", checkInsHoy);
                model.addAttribute("checkOutsHoy", checkOutsHoy);
                model.addAttribute("ultimasReservas", reservaService.obtenerUltimasReservas(5));
                model.addAttribute("ingresosUltimos30Dias", reservaService.getIngresosUltimosDias(30));

                // Contador de notificaciones no leídas
                long notificacionesNoLeidas = notificacionService.obtenerNoLeidas().size();
                model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
                model.addAttribute("notificaciones", notificacionService.obtenerTodas());

                return "dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar estadísticas");
        }

        return "redirect:/login";
    }
}
