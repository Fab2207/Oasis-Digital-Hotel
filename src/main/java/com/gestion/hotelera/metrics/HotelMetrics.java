package com.gestion.hotelera.metrics;

import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ClienteRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Métricas personalizadas del negocio hotelero
 * Estas métricas se exponen en /actuator/metrics
 */
@Component
public class HotelMetrics {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ClienteRepository clienteRepository;

    public HotelMetrics(MeterRegistry meterRegistry,
            ReservaRepository reservaRepository,
            HabitacionRepository habitacionRepository,
            ClienteRepository clienteRepository) {
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
        this.clienteRepository = clienteRepository;

        // Registrar todas las métricas personalizadas
        registrarMetricasReservas(meterRegistry);
        registrarMetricasHabitaciones(meterRegistry);
        registrarMetricasClientes(meterRegistry);
        registrarMetricasIngresos(meterRegistry);
    }

    private void registrarMetricasReservas(MeterRegistry registry) {
        // Total de reservas
        Gauge.builder("hotel.reservas.total", reservaRepository, repo -> repo.count())
                .description("Número total de reservas en el sistema")
                .register(registry);

        // Reservas activas
        Gauge.builder("hotel.reservas.activas", reservaRepository,
                repo -> repo.countByEstadoReservaIgnoreCase("ACTIVA"))
                .description("Número de reservas actualmente activas")
                .tag("estado", "ACTIVA")
                .register(registry);

        // Reservas pendientes
        Gauge.builder("hotel.reservas.pendientes", reservaRepository,
                repo -> repo.countByEstadoReservaIgnoreCase("PENDIENTE"))
                .description("Número de reservas pendientes")
                .tag("estado", "PENDIENTE")
                .register(registry);

        // Reservas finalizadas
        Gauge.builder("hotel.reservas.finalizadas", reservaRepository,
                repo -> repo.countByEstadoReservaIgnoreCase("FINALIZADA"))
                .description("Número de reservas finalizadas")
                .tag("estado", "FINALIZADA")
                .register(registry);

        // Check-ins de hoy
        Gauge.builder("hotel.checkins.hoy", reservaRepository,
                repo -> repo.countByFechaInicio(LocalDate.now()))
                .description("Número de check-ins programados para hoy")
                .register(registry);

        // Check-outs de hoy
        Gauge.builder("hotel.checkouts.hoy", reservaRepository,
                repo -> repo.countByFechaFin(LocalDate.now()))
                .description("Número de check-outs programados para hoy")
                .register(registry);
    }

    private void registrarMetricasHabitaciones(MeterRegistry registry) {
        // Total de habitaciones
        Gauge.builder("hotel.habitaciones.total", habitacionRepository, repo -> repo.count())
                .description("Número total de habitaciones en el hotel")
                .register(registry);

        // Habitaciones disponibles
        Gauge.builder("hotel.habitaciones.disponibles", habitacionRepository,
                repo -> repo.countByEstadoIgnoreCase("DISPONIBLE"))
                .description("Número de habitaciones disponibles")
                .tag("estado", "DISPONIBLE")
                .register(registry);

        // Habitaciones ocupadas
        Gauge.builder("hotel.habitaciones.ocupadas", habitacionRepository,
                repo -> repo.countByEstadoIgnoreCase("OCUPADA"))
                .description("Número de habitaciones ocupadas")
                .tag("estado", "OCUPADA")
                .register(registry);

        // Habitaciones en mantenimiento
        Gauge.builder("hotel.habitaciones.mantenimiento", habitacionRepository,
                repo -> repo.countByEstadoIgnoreCase("MANTENIMIENTO"))
                .description("Número de habitaciones en mantenimiento")
                .tag("estado", "MANTENIMIENTO")
                .register(registry);

        // Tasa de ocupación (porcentaje)
        Gauge.builder("hotel.habitaciones.ocupacion.porcentaje", this,
                metrics -> calcularTasaOcupacion())
                .description("Porcentaje de ocupación del hotel")
                .baseUnit("percent")
                .register(registry);
    }

    private void registrarMetricasClientes(MeterRegistry registry) {
        // Total de clientes registrados
        Gauge.builder("hotel.clientes.total", clienteRepository, repo -> repo.count())
                .description("Número total de clientes registrados")
                .register(registry);
    }

    private void registrarMetricasIngresos(MeterRegistry registry) {
        // Ingresos totales (reservas finalizadas)
        Gauge.builder("hotel.ingresos.total", this, metrics -> calcularIngresosTotales())
                .description("Ingresos totales de reservas finalizadas")
                .baseUnit("soles")
                .register(registry);
    }

    /**
     * Calcula la tasa de ocupación del hotel
     */
    private double calcularTasaOcupacion() {
        long totalHabitaciones = habitacionRepository.count();
        if (totalHabitaciones == 0) {
            return 0.0;
        }

        long habitacionesOcupadas = habitacionRepository.countByEstadoIgnoreCase("OCUPADA");
        return (habitacionesOcupadas * 100.0) / totalHabitaciones;
    }

    /**
     * Calcula los ingresos totales de reservas finalizadas
     */
    private double calcularIngresosTotales() {
        return reservaRepository.findAll().stream()
                .filter(r -> "FINALIZADA".equalsIgnoreCase(r.getEstadoReserva()))
                .mapToDouble(r -> r.getTotalPagar() != null ? r.getTotalPagar() : 0.0)
                .sum();
    }
}
