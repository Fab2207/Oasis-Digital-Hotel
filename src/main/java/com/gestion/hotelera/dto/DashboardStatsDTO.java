package com.gestion.hotelera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalHabitaciones;
    private long totalClientes;
    private long totalReservas;
    private long habitacionesDisponibles;
    private long habitacionesOcupadas;
    private long habitacionesMantenimiento;
    private double ingresosTotales;
    private long reservasPendientes;
    private long reservasActivas;
    private long checkInsHoy;
    private long checkOutsHoy;
    private List<Map<String, Object>> ingresosUltimos30Dias;
}
