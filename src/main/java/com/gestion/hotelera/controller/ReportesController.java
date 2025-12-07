package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ReportesController {

    private final ReservaService reservaService;
    private final HabitacionService habitacionService;

    public ReportesController(ReservaService reservaService, HabitacionService habitacionService) {
        this.reservaService = reservaService;
        this.habitacionService = habitacionService;
    }

    @GetMapping
    public String mostrarReportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        // Obtener todas las reservas
        List<Reserva> reservas = reservaService.obtenerTodasLasReservas();

        // Default to last 30 days if no dates provided
        if (fechaInicio == null || fechaFin == null) {
            fechaFin = LocalDate.now();
            fechaInicio = fechaFin.minusDays(30);
        }

        // Filtrar por fechas
        final LocalDate start = fechaInicio;
        final LocalDate end = fechaFin;

        reservas = reservas.stream()
                .filter(r -> (r.getFechaInicio().isEqual(start) || r.getFechaInicio().isAfter(start)) &&
                        (r.getFechaFin().isEqual(end) || r.getFechaFin().isBefore(end)))
                .collect(Collectors.toList());

        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        // Calcular KPIs basados en las reservas filtradas
        double ingresosTotales = reservas.stream()
                .filter(r -> "FINALIZADA".equals(r.getEstadoReserva()) || "ACTIVA".equals(r.getEstadoReserva()))
                .mapToDouble(Reserva::calcularTotalConDescuento)
                .sum();

        long totalHabitaciones = habitacionService.contarHabitaciones();

        // Ocupación basada en reservas activas en el periodo (aproximación)
        long habitacionesOcupadas = reservas.stream()
                .filter(r -> "ACTIVA".equals(r.getEstadoReserva()))
                .map(Reserva::getHabitacion)
                .distinct()
                .count();

        double tasaOcupacion = totalHabitaciones > 0 ? (double) habitacionesOcupadas / totalHabitaciones * 100 : 0.0;

        long reservasFinalizadas = reservas.stream().filter(r -> "FINALIZADA".equals(r.getEstadoReserva())).count();
        double adr = reservasFinalizadas > 0 ? ingresosTotales / reservasFinalizadas : 0.0;

        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("tasaOcupacion", String.format("%.1f", tasaOcupacion));
        model.addAttribute("adr", String.format("%.2f", adr));

        return "reportes";
    }

    @GetMapping("/generar")
    public String mostrarFormularioGenerarReporte(Model model) {
        model.addAttribute("fechaActual", LocalDate.now());
        model.addAttribute("currentPath", "/reportes/generar");
        return "generar_reporte";
    }

    @GetMapping("/api/ingresos")
    @ResponseBody
    public List<Map<String, Object>> getIngresosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return List.of();
            }
            if (fechaInicio.isAfter(fechaFin)) {
                return List.of();
            }
            return reservaService.getIngresosPorPeriodo(fechaInicio, fechaFin);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/api/movimiento")
    @ResponseBody
    public List<Map<String, Object>> getMovimientoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return List.of();
            }
            if (fechaInicio.isAfter(fechaFin)) {
                return List.of();
            }
            return reservaService.getMovimientoPorPeriodo(fechaInicio, fechaFin);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/api/ocupacion")
    @ResponseBody
    public List<Map<String, Object>> getOcupacionPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return List.of();
            }
            if (fechaInicio.isAfter(fechaFin)) {
                return List.of();
            }
            return reservaService.getOcupacionDiariaPorPeriodo(fechaInicio, fechaFin);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/exportar-pdf")
    public String exportarPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        // Reutilizar lógica de filtrado (idealmente refactorizar a un servicio privado)
        List<Reserva> reservas = reservaService.obtenerTodasLasReservas();

        if (fechaInicio == null || fechaFin == null) {
            fechaFin = LocalDate.now();
            fechaInicio = fechaFin.minusDays(30);
        }

        final LocalDate start = fechaInicio;
        final LocalDate end = fechaFin;

        reservas = reservas.stream()
                .filter(r -> (r.getFechaInicio().isEqual(start) || r.getFechaInicio().isAfter(start)) &&
                        (r.getFechaFin().isEqual(end) || r.getFechaFin().isBefore(end)))
                .collect(Collectors.toList());

        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("reservas", reservas); // Pasar lista de reservas para la tabla

        double ingresosTotales = reservas.stream()
                .filter(r -> "FINALIZADA".equals(r.getEstadoReserva()) || "ACTIVA".equals(r.getEstadoReserva()))
                .mapToDouble(Reserva::calcularTotalConDescuento)
                .sum();

        long totalHabitaciones = habitacionService.contarHabitaciones();
        long habitacionesOcupadas = reservas.stream()
                .filter(r -> "ACTIVA".equals(r.getEstadoReserva()))
                .map(Reserva::getHabitacion)
                .distinct()
                .count();

        double tasaOcupacion = totalHabitaciones > 0 ? (double) habitacionesOcupadas / totalHabitaciones * 100 : 0.0;
        long reservasFinalizadas = reservas.stream().filter(r -> "FINALIZADA".equals(r.getEstadoReserva())).count();
        double adr = reservasFinalizadas > 0 ? ingresosTotales / reservasFinalizadas : 0.0;

        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("tasaOcupacion", String.format("%.1f", tasaOcupacion));
        model.addAttribute("adr", String.format("%.2f", adr));

        return "reporte-impresion";
    }
}