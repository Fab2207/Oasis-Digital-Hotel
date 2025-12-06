package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCliente(Cliente cliente);
    List<Reserva> findByHabitacion(Habitacion habitacion);
    List<Reserva> findByClienteAndEstadoReserva(Cliente cliente, String estadoReserva);
    boolean existsByClienteAndEstadoReservaIn(Cliente cliente, List<String> estadosActivos);
    long countByFechaInicioAndEstadoReserva(LocalDate fechaInicio, String estadoReserva);
    long countByFechaFinAndEstadoReserva(LocalDate fechaFin, String estadoReserva);
    long countByFechaInicioAndEstadoReservaIn(LocalDate fechaInicio, List<String> estadoReserva);
    long countByFechaFinAndEstadoReservaIn(LocalDate fechaFin, List<String> estadoReserva);
    long countByEstadoReserva(String estadoReserva);
    List<Reserva> findByEstadoReserva(String estadoReserva);
    long countByFechaSalidaReal(LocalDate fechaSalidaReal);
    List<Reserva> findByEstadoReservaAndFechaSalidaRealBetween(String estado, LocalDate fechaInicio, LocalDate fechaFin);

    long countByEstadoReservaIgnoreCase(String estado);
    long countByFechaInicio(LocalDate fechaInicio);
    long countByFechaFin(LocalDate fechaFin);

    @Query("SELECT COUNT(DISTINCT r.habitacion.id) FROM Reserva r WHERE " +
            "(r.fechaInicio <= :date AND r.fechaFin >= :date AND r.estadoReserva IN ('ACTIVA', 'PENDIENTE'))")
    long countActiveReservationsOnDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(r.totalPagar), 0.0) FROM Reserva r WHERE r.estadoReserva IN ('PENDIENTE', 'ACTIVA', 'FINALIZADA')")
    Double sumTotalPagarForPendingActiveAndFinalizedReservas();

    @Query("SELECT DISTINCT r.habitacion.id FROM Reserva r WHERE r.cliente.id = :clienteId AND r.estadoReserva IN ('PENDIENTE', 'ACTIVA')")
    List<Long> findHabitacionesReservadasPorCliente(@Param("clienteId") Long clienteId);

    /**
     * Verifica si existe una reserva activa o pendiente para una habitación en un rango de fechas
     * Excluye la reserva actual si se está actualizando (reservaId != null)
     */
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.habitacion.id = :habitacionId " +
            "AND r.estadoReserva IN ('PENDIENTE', 'ACTIVA') " +
            "AND (:fechaInicio < r.fechaFin AND :fechaFin > r.fechaInicio) " +
            "AND (:reservaId IS NULL OR r.id != :reservaId)")
    boolean existeReservaEnRangoFechas(@Param("habitacionId") Long habitacionId,
                                       @Param("fechaInicio") LocalDate fechaInicio,
                                       @Param("fechaFin") LocalDate fechaFin,
                                       @Param("reservaId") Long reservaId);

    /**
     * Encuentra todas las reservas activas o pendientes que están dentro de un rango de fechas
     */
    @Query("SELECT r FROM Reserva r WHERE r.habitacion.id = :habitacionId " +
            "AND r.estadoReserva IN ('PENDIENTE', 'ACTIVA') " +
            "AND (:fechaInicio < r.fechaFin AND :fechaFin > r.fechaInicio) " +
            "AND (:reservaId IS NULL OR r.id != :reservaId)")
    List<Reserva> findReservasConflictivas(@Param("habitacionId") Long habitacionId,
                                           @Param("fechaInicio") LocalDate fechaInicio,
                                           @Param("fechaFin") LocalDate fechaFin,
                                           @Param("reservaId") Long reservaId);
}
