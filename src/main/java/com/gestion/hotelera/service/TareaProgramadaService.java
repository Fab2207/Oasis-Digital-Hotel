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

    @Scheduled(fixedRate = 3600000) 
    @Transactional
    public void sincronizarReservasYHabitaciones() {
        try {
            LocalDate hoy = LocalDate.now();
            logger.info("Iniciando sincronización de reservas y habitaciones - Fecha actual: {}", hoy);

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

                    if (reserva.getHabitacion() != null && habitacionService != null) {
                        habitacionService.actualizarEstadoHabitacion(
                                reserva.getHabitacion().getId(),
                                "DISPONIBLE");
                    }

                    reservasFinalizadas++;
                    logger.info("Reserva ID {} finalizada automáticamente (fecha fin: {})",
                            reserva.getId(), reserva.getFechaFin());
                } catch (Exception e) {
                    logger.error("Error al finalizar reserva ID {}: {}", reserva.getId(), e.getMessage());
                }
            }

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

                    if (reserva.getHabitacion() != null && habitacionService != null) {
                        habitacionService.actualizarEstadoHabitacion(
                                reserva.getHabitacion().getId(),
                                "OCUPADA");
                    }

                    reservasActivadas++;
                    logger.info("Reserva ID {} activada automáticamente (fecha inicio: {})",
                            reserva.getId(), reserva.getFechaInicio());
                } catch (Exception e) {
                    logger.error("Error al activar reserva ID {}: {}", reserva.getId(), e.getMessage());
                }
            }

            sincronizarEstadosHabitaciones();

            logger.info("Sincronización completada - Finalizadas: {}, Activadas: {}",
                    reservasFinalizadas, reservasActivadas);

        } catch (Exception e) {
            logger.error("Error en la sincronización de reservas y habitaciones: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") 
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

    @Transactional
    public void sincronizarEstadosHabitaciones() {
        if (habitacionService == null) {
            return;
        }

        try {
            LocalDate hoy = LocalDate.now();

            var todasHabitaciones = habitacionService.obtenerTodasLasHabitaciones();

            List<Reserva> reservasActivas = reservaRepository.findAll().stream()
                    .filter(r -> (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                            r.getHabitacion() != null &&
                            r.getFechaInicio() != null &&
                            r.getFechaFin() != null)
                    .toList();

            for (var habitacion : todasHabitaciones) {
                boolean deberiaEstarOcupada = reservasActivas.stream()
                        .anyMatch(r -> r.getHabitacion().getId().equals(habitacion.getId())
                                && ("ACTIVA".equalsIgnoreCase(r.getEstadoReserva()) ||
                                        ("PENDIENTE".equalsIgnoreCase(r.getEstadoReserva()) &&
                                                !r.getFechaInicio().isAfter(hoy) &&
                                                !r.getFechaFin().isBefore(hoy))));

                String estadoActual = habitacion.getEstado();
                String estadoEsperado = deberiaEstarOcupada ? "OCUPADA" : "DISPONIBLE";

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
