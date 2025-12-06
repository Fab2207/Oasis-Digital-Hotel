package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para tareas programadas que mantienen la sincronización
 * entre reservas y estados de habitaciones.
 */
@Service
public class TareaProgramadaService {

    private static final Logger logger = LoggerFactory.getLogger(TareaProgramadaService.class);

    private final ReservaRepository reservaRepository;
    private final HabitacionService habitacionService;

    public TareaProgramadaService(ReservaRepository reservaRepository,
                                  HabitacionService habitacionService) {
        this.reservaRepository = reservaRepository;
        this.habitacionService = habitacionService;
    }

    /**
     * Tarea programada que se ejecuta cada hora para:
     * 1. Finalizar reservas que han pasado su fecha de fin
     * 2. Activar reservas pendientes cuya fecha de inicio ha llegado
     * 3. Actualizar estados de habitaciones según las reservas
     */
    @Scheduled(fixedRate = 3600000) // Cada hora (3600000 ms = 1 hora)
    @Transactional
    public void sincronizarReservasYHabitaciones() {
        try {
            LocalDate hoy = LocalDate.now();
            logger.info("Iniciando sincronización de reservas y habitaciones - Fecha actual: {}", hoy);

            // 1. Finalizar reservas que han pasado su fecha de fin
            List<Reserva> reservasParaFinalizar = reservaRepository.findAll().stream()
                    .filter(r -> (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                            r.getFechaFin() != null &&
                            r.getFechaFin().isBefore(hoy))
                    .toList();

            int reservasFinalizadas = 0;
            for (Reserva reserva : reservasParaFinalizar) {
                try {
                    reserva.setEstadoReserva("FINALIZADA");
                    reservaRepository.save(reserva);

                    // Liberar habitación
                    if (reserva.getHabitacion() != null && habitacionService != null) {
                        habitacionService.actualizarEstadoHabitacion(
                                reserva.getHabitacion().getId(),
                                "DISPONIBLE"
                        );
                    }

                    reservasFinalizadas++;
                    logger.info("Reserva ID {} finalizada automáticamente (fecha fin: {})",
                            reserva.getId(), reserva.getFechaFin());
                } catch (Exception e) {
                    logger.error("Error al finalizar reserva ID {}: {}", reserva.getId(), e.getMessage());
                }
            }

            // 2. Activar reservas pendientes cuya fecha de inicio ha llegado
            List<Reserva> reservasParaActivar = reservaRepository.findAll().stream()
                    .filter(r -> r.getEstadoReserva().equals("PENDIENTE") &&
                            r.getFechaInicio() != null &&
                            (r.getFechaInicio().isBefore(hoy) || r.getFechaInicio().equals(hoy)))
                    .toList();

            int reservasActivadas = 0;
            for (Reserva reserva : reservasParaActivar) {
                try {
                    reserva.setEstadoReserva("ACTIVA");
                    reservaRepository.save(reserva);

                    // Marcar habitación como ocupada
                    if (reserva.getHabitacion() != null && habitacionService != null) {
                        habitacionService.actualizarEstadoHabitacion(
                                reserva.getHabitacion().getId(),
                                "OCUPADA"
                        );
                    }

                    reservasActivadas++;
                    logger.info("Reserva ID {} activada automáticamente (fecha inicio: {})",
                            reserva.getId(), reserva.getFechaInicio());
                } catch (Exception e) {
                    logger.error("Error al activar reserva ID {}: {}", reserva.getId(), e.getMessage());
                }
            }

            // 3. Sincronizar estados de habitaciones según reservas activas
            sincronizarEstadosHabitaciones();

            logger.info("Sincronización completada - Finalizadas: {}, Activadas: {}",
                    reservasFinalizadas, reservasActivadas);

        } catch (Exception e) {
            logger.error("Error en la sincronización de reservas y habitaciones: {}", e.getMessage(), e);
        }
    }

    /**
     * Tarea programada que se ejecuta diariamente a las 2 AM para una limpieza más exhaustiva
     */
    @Scheduled(cron = "0 0 2 * * ?") // Todos los días a las 2:00 AM
    @Transactional
    public void limpiezaDiariaReservasYHabitaciones() {
        try {
            logger.info("Iniciando limpieza diaria de reservas y habitaciones - {}", LocalDateTime.now());
            sincronizarReservasYHabitaciones();
            sincronizarEstadosHabitaciones();
            logger.info("Limpieza diaria completada");
        } catch (Exception e) {
            logger.error("Error en la limpieza diaria: {}", e.getMessage(), e);
        }
    }

    /**
     * Sincroniza los estados de las habitaciones según las reservas activas
     * Esto asegura que las habitaciones reflejen correctamente su disponibilidad
     */
    @Transactional
    public void sincronizarEstadosHabitaciones() {
        if (habitacionService == null) {
            return;
        }

        try {
            LocalDate hoy = LocalDate.now();

            // Obtener todas las habitaciones
            var todasHabitaciones = habitacionService.obtenerTodasLasHabitaciones();

            // Obtener todas las reservas activas o pendientes
            List<Reserva> reservasActivas = reservaRepository.findAll().stream()
                    .filter(r -> (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                            r.getHabitacion() != null &&
                            r.getFechaInicio() != null &&
                            r.getFechaFin() != null)
                    .toList();

            // Para cada habitación, verificar si debería estar ocupada
            for (var habitacion : todasHabitaciones) {
                boolean deberiaEstarOcupada = reservasActivas.stream()
                        .anyMatch(r -> r.getHabitacion().getId().equals(habitacion.getId()) &&
                                !r.getFechaInicio().isAfter(hoy) &&
                                !r.getFechaFin().isBefore(hoy));

                String estadoActual = habitacion.getEstado();
                String estadoEsperado = deberiaEstarOcupada ? "OCUPADA" : "DISPONIBLE";

                // Solo actualizar si hay una discrepancia (y no está en mantenimiento)
                if (!estadoActual.equals(estadoEsperado) && !"MANTENIMIENTO".equalsIgnoreCase(estadoActual)) {
                    habitacionService.actualizarEstadoHabitacion(habitacion.getId(), estadoEsperado);
                    logger.debug("Habitación {} sincronizada: {} -> {}",
                            habitacion.getNumero(), estadoActual, estadoEsperado);
                }
            }
        } catch (Exception e) {
            logger.error("Error al sincronizar estados de habitaciones: {}", e.getMessage(), e);
        }
    }
}

