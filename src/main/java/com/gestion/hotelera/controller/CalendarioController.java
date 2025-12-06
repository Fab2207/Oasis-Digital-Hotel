package com.gestion.hotelera.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calendario")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
public class CalendarioController {

    private final com.gestion.hotelera.service.HabitacionService habitacionService;
    private final com.gestion.hotelera.service.ReservaService reservaService;

    public CalendarioController(com.gestion.hotelera.service.HabitacionService habitacionService,
            com.gestion.hotelera.service.ReservaService reservaService) {
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
    }

    @GetMapping
    public String mostrarCalendario(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String date,
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "month") String view,
            Model model) {

        java.time.LocalDate selectedDate;
        try {
            selectedDate = (date != null && !date.isEmpty())
                    ? java.time.LocalDate.parse(date)
                    : java.time.LocalDate.now();
        } catch (Exception e) {
            selectedDate = java.time.LocalDate.now();
        }

        java.time.LocalDate start;
        java.time.LocalDate end;
        String title;

        if ("week".equalsIgnoreCase(view)) {
            // Start of week (Monday)
            start = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            end = start.plusDays(6);
            title = "Semana del " + start.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.forLanguageTag("es-ES")));
        } else if ("day".equalsIgnoreCase(view)) {
            start = selectedDate;
            end = selectedDate;
            title = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy",
                    java.util.Locale.forLanguageTag("es-ES")));
        } else {
            // Month view (default)
            java.time.YearMonth yearMonth = java.time.YearMonth.from(selectedDate);
            start = yearMonth.atDay(1);
            end = yearMonth.atEndOfMonth();
            title = yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                    java.util.Locale.forLanguageTag("es-ES")));
        }

        java.time.LocalDate now = java.time.LocalDate.now();

        // 1. Días del mes
        java.util.List<java.util.Map<String, Object>> diasMes = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE dd",
                java.util.Locale.forLanguageTag("es-ES"));

        for (java.time.LocalDate iterDate = start; !iterDate.isAfter(end); iterDate = iterDate.plusDays(1)) {
            java.util.Map<String, Object> diaInfo = new java.util.HashMap<>();
            diaInfo.put("fecha", iterDate.toString());
            diaInfo.put("nombre", iterDate.format(dayFormatter));
            diaInfo.put("esHoy", iterDate.equals(now));
            diasMes.add(diaInfo);
        }

        // 2. Habitaciones
        java.util.List<com.gestion.hotelera.model.Habitacion> habitaciones = habitacionService
                .obtenerTodasLasHabitaciones();

        // 3. Mapa de disponibilidad: Map<HabitacionID, Map<Fecha, Object>>
        // Object puede ser un String ("LIBRE") o un Map con detalles de la reserva
        java.util.Map<Long, java.util.Map<String, Object>> disponibilidad = new java.util.HashMap<>();

        java.util.List<com.gestion.hotelera.model.Reserva> reservasMes = reservaService.obtenerReservasPorPeriodo(start,
                end);

        for (com.gestion.hotelera.model.Habitacion hab : habitaciones) {
            java.util.Map<String, Object> diasEstado = new java.util.HashMap<>();

            // Inicializar como libre
            for (java.time.LocalDate iterDate = start; !iterDate.isAfter(end); iterDate = iterDate.plusDays(1)) {
                java.util.Map<String, Object> estadoLibre = new java.util.HashMap<>();
                estadoLibre.put("estado", "LIBRE");
                diasEstado.put(iterDate.toString(), estadoLibre);
            }

            // Marcar ocupadas por reservas
            for (com.gestion.hotelera.model.Reserva res : reservasMes) {
                if (res.getHabitacion().getId().equals(hab.getId()) &&
                        !res.getEstadoReserva().equals("CANCELADA")) { // Mostrar todas menos canceladas

                    java.time.LocalDate resStart = res.getFechaInicio();
                    java.time.LocalDate resEnd = res.getFechaFin();
                    String nombreCliente = res.getCliente() != null
                            ? res.getCliente().getNombres() + " " + res.getCliente().getApellidos()
                            : "Cliente";

                    for (java.time.LocalDate iterDate = start; !iterDate.isAfter(end); iterDate = iterDate
                            .plusDays(1)) {
                        if (!iterDate.isBefore(resStart) && !iterDate.isAfter(resEnd)) {
                            java.util.Map<String, Object> estadoOcupado = new java.util.HashMap<>();
                            estadoOcupado.put("estado", res.getEstadoReserva()); // Usar el estado real (ACTIVA,
                                                                                 // FINALIZADA, ETC)
                            estadoOcupado.put("tipo", res.getEstadoReserva());
                            estadoOcupado.put("cliente", nombreCliente);
                            estadoOcupado.put("reservaId", res.getId());
                            diasEstado.put(iterDate.toString(), estadoOcupado);
                        }
                    }
                }
            }

            // Override con estado actual de la habitación si es relevante
            if ("MANTENIMIENTO".equals(hab.getEstado())) {
                java.util.Map<String, Object> estadoMantenimiento = new java.util.HashMap<>();
                estadoMantenimiento.put("estado", "MANTENIMIENTO");
                diasEstado.put(now.toString(), estadoMantenimiento);
            }

            disponibilidad.put(hab.getId(), diasEstado);
        }

        model.addAttribute("diasMes", diasMes);
        model.addAttribute("habitaciones", habitaciones);
        model.addAttribute("disponibilidad", disponibilidad);

        // Agregar reservas de hoy (llegadas)
        model.addAttribute("reservasHoy", reservaService.obtenerLlegadasHoy());
        model.addAttribute("mesAnio", title);
        model.addAttribute("view", view);
        model.addAttribute("currentDate", selectedDate.toString());

        return "calendario";
    }
}
