package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.DashboardStatsDTO;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DashboardApiController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final HabitacionService habitacionService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long totalHabitaciones = habitacionService.contarHabitaciones();
        long totalClientes = clienteService.contarClientes();
        long totalReservas = reservaService.contarReservas();
        long habitacionesMantenimiento = habitacionService.contarEnMantenimiento();
        long habitacionesOcupadas = reservaService.contarHabitacionesReservadas();

        long habitacionesDisponibles = Math.max(0,
                totalHabitaciones - habitacionesOcupadas - habitacionesMantenimiento);

        double ingresosTotales = reservaService.calcularIngresosTotales();
        long reservasPendientes = reservaService.contarReservasPorEstado("PENDIENTE");
        long reservasActivas = reservaService.contarReservasPorEstado("ACTIVA");

        long checkInsHoy = reservaService.contarCheckInsHoy();
        long checkOutsHoy = reservaService.contarCheckOutsHoy();

        LocalDate hoy = LocalDate.now();
        LocalDate hace30dias = hoy.minusDays(30);

        var ingresosUltimos30Dias = reservaService.getIngresosPorPeriodo(hace30dias, hoy);

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalHabitaciones(totalHabitaciones)
                .totalClientes(totalClientes)
                .totalReservas(totalReservas)
                .habitacionesDisponibles(habitacionesDisponibles)
                .habitacionesOcupadas(habitacionesOcupadas)
                .habitacionesMantenimiento(habitacionesMantenimiento)
                .ingresosTotales(ingresosTotales)
                .reservasPendientes(reservasPendientes)
                .reservasActivas(reservasActivas)
                .checkInsHoy(checkInsHoy)
                .checkOutsHoy(checkOutsHoy)
                .ingresosUltimos30Dias(ingresosUltimos30Dias)
                .build());
    }
}
