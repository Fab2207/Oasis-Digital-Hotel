package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para enviar emails automatizados mediante tareas programadas
 */
@Service
public class ScheduledEmailService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEmailService.class);

    private final ReservaRepository reservaRepository;
    private final EmailService emailService;

    public ScheduledEmailService(ReservaRepository reservaRepository, EmailService emailService) {
        this.reservaRepository = reservaRepository;
        this.emailService = emailService;
    }

    /**
     * Tarea programada que se ejecuta todos los días a las 9:00 AM
     * Envía recordatorios de check-in a clientes con reservas para mañana
     */
    @Scheduled(cron = "0 0 9 * * ?") // Ejecutar a las 9:00 AM todos los días
    public void enviarRecordatoriosCheckIn() {
        log.info("=== INICIANDO ENVÍO DE RECORDATORIOS DE CHECK-IN ===");

        try {
            LocalDate manana = LocalDate.now().plusDays(1);

            // Buscar todas las reservas pendientes que tienen check-in mañana
            List<Reserva> reservasMaana = reservaRepository.findAll().stream()
                    .filter(r -> "PENDIENTE".equalsIgnoreCase(r.getEstadoReserva()))
                    .filter(r -> r.getFechaInicio() != null && r.getFechaInicio().equals(manana))
                    .filter(r -> r.getCliente() != null && r.getCliente().getEmail() != null)
                    .toList();

            log.info("Se encontraron {} reservas con check-in mañana ({})",
                    reservasMaana.size(), manana);

            int enviados = 0;
            for (Reserva reserva : reservasMaana) {
                try {
                    String to = reserva.getCliente().getEmail();
                    if (to != null && !to.trim().isEmpty()) {
                        String nombre = reserva.getCliente().getNombres();
                        String numeroReserva = String.valueOf(reserva.getId());
                        String fechaCheckIn = reserva.getFechaInicio().toString();
                        String habitacion = reserva.getHabitacion() != null
                                ? reserva.getHabitacion().getNumero()
                                : "Por asignar";

                        emailService.enviarRecordatorioCheckIn(to, nombre, numeroReserva,
                                fechaCheckIn, habitacion);
                        enviados++;

                        log.info("Recordatorio enviado a {} para reserva #{}", to, numeroReserva);
                    }
                } catch (Exception e) {
                    log.error("Error al enviar recordatorio para reserva #{}",
                            reserva.getId(), e);
                }
            }

            log.info("=== RECORDATORIOS COMPLETADOS: {}/{} enviados ===",
                    enviados, reservasMaana.size());

        } catch (Exception e) {
            log.error("Error en la tarea programada de recordatorios", e);
        }
    }

    /**
     * Tarea programada que actualiza el estado de las habitaciones
     * Se ejecuta cada 6 horas para asegurar sincronización
     */
    @Scheduled(cron = "0 0 */6 * * ?") // Ejecutar cada 6 horas
    public void actualizarEstadosReservas() {
        log.info("=== VERIFICANDO ESTADOS DE RESERVAS ===");

        try {
            LocalDate hoy = LocalDate.now();

            // Buscar reservas que deberían estar finalizadas
            List<Reserva> reservasVencidas = reservaRepository.findAll().stream()
                    .filter(r -> "ACTIVA".equalsIgnoreCase(r.getEstadoReserva()) ||
                            "PENDIENTE".equalsIgnoreCase(r.getEstadoReserva()))
                    .filter(r -> r.getFechaFin() != null && r.getFechaFin().isBefore(hoy))
                    .toList();

            if (!reservasVencidas.isEmpty()) {
                log.warn("Se encontraron {} reservas vencidas que deberían estar finalizadas",
                        reservasVencidas.size());

                for (Reserva reserva : reservasVencidas) {
                    log.warn("Reserva #{} vencida: fecha fin {} pero estado '{}'",
                            reserva.getId(),
                            reserva.getFechaFin(),
                            reserva.getEstadoReserva());
                }
            }

            log.info("=== VERIFICACIÓN COMPLETADA ===");

        } catch (Exception e) {
            log.error("Error en la tarea de verificación de estados", e);
        }
    }
}
