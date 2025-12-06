package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class HabitacionService {

    private static final Logger logger = LoggerFactory.getLogger(HabitacionService.class);
    // Re-validated by system
    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final AuditoriaService auditoriaService;

    public HabitacionService(HabitacionRepository habitacionRepository,
            ReservaRepository reservaRepository,
            AuditoriaService auditoriaService) {
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.auditoriaService = auditoriaService;
    }

    // Constructor para tests eliminado - Spring necesita un solo constructor
    // público
    // Para tests, usa @MockBean o @TestConfiguration en lugar de un constructor
    // adicional

    @Transactional
    public void inicializarHabitacionesSiNoExisten() {
        if (habitacionRepository.count() == 0) {
            crearHabitacionesIniciales();
        }
    }

    private void crearHabitacionesIniciales() {
        // 10 Habitaciones en total
        // Simple: 50
        habitacionRepository.save(new Habitacion("101", "Simple", 50.0, "DISPONIBLE"));
        habitacionRepository.save(new Habitacion("102", "Simple", 50.0, "DISPONIBLE"));

        // Doble: 80
        habitacionRepository.save(new Habitacion("103", "Doble", 80.0, "DISPONIBLE"));
        habitacionRepository.save(new Habitacion("104", "Doble", 80.0, "DISPONIBLE"));

        // Matrimonial: 120
        habitacionRepository.save(new Habitacion("201", "Matrimonial", 120.0, "DISPONIBLE"));
        habitacionRepository.save(new Habitacion("202", "Matrimonial", 120.0, "DISPONIBLE"));

        // Suite Junior: 150
        habitacionRepository.save(new Habitacion("203", "Suite Junior", 150.0, "DISPONIBLE"));
        habitacionRepository.save(new Habitacion("204", "Suite Junior", 150.0, "DISPONIBLE"));

        // Suite Presidencial: 200
        habitacionRepository.save(new Habitacion("205", "Suite Presidencial", 200.0, "DISPONIBLE"));
        habitacionRepository.save(new Habitacion("206", "Suite Presidencial", 200.0, "DISPONIBLE"));

        logger.info("Habitaciones iniciales creadas: 10 habitaciones");
    }

    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    public Optional<Habitacion> buscarHabitacionPorId(Long id) {
        return habitacionRepository.findById(id);
    }

    @Transactional
    public Habitacion crearHabitacion(Habitacion habitacion) {
        if (habitacionRepository.findByNumero(habitacion.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el número: " + habitacion.getNumero());
        }
        Habitacion guardada = habitacionRepository.save(habitacion);
        auditoriaService.registrarAccion("CREACION_HABITACION",
                "Nueva habitación: " + guardada.getNumero(), "Habitacion", guardada.getId());
        return guardada;
    }

    @Transactional
    public void actualizarEstadoHabitacion(Long id, String nuevoEstado) {
        Optional<Habitacion> opt = habitacionRepository.findById(id);
        if (opt.isPresent()) {
            Habitacion habitacion = opt.get();
            String estadoAnterior = habitacion.getEstado();
            habitacion.setEstado(nuevoEstado);
            habitacionRepository.save(habitacion);
            auditoriaService.registrarAccion("CAMBIO_ESTADO_HABITACION",
                    "Habitación " + habitacion.getNumero() + " cambió de " + estadoAnterior + " a " + nuevoEstado,
                    "Habitacion", id);
        }
    }

    public boolean estaDisponible(Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Habitacion> habitacionOpt = habitacionRepository.findById(habitacionId);
        if (habitacionOpt.isEmpty()) {
            return false;
        }

        Habitacion habitacion = habitacionOpt.get();
        if ("MANTENIMIENTO".equals(habitacion.getEstado())) {
            return false;
        }

        List<Reserva> reservasConflictivas = reservaRepository.findAll().stream()
                .filter(r -> r.getHabitacion() != null && r.getHabitacion().getId().equals(habitacionId))
                .filter(r -> "ACTIVA".equals(r.getEstadoReserva()) || "PENDIENTE".equals(r.getEstadoReserva())
                        || "PROCESANDO".equals(r.getEstadoReserva()))
                .filter(r -> !(fechaFin.isBefore(r.getFechaInicio()) || fechaInicio.isAfter(r.getFechaFin())))
                .collect(Collectors.toList());

        return reservasConflictivas.isEmpty();
    }

    public long contarHabitaciones() {
        return habitacionRepository.count();
    }

    public long contarDisponibles() {
        return habitacionRepository.countByEstado("DISPONIBLE");
    }

    public long contarOcupadas() {
        return habitacionRepository.countByEstado("OCUPADA");
    }

    public long contarEnMantenimiento() {
        return habitacionRepository.countByEstado("MANTENIMIENTO");
    }

    public List<Habitacion> buscarDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        return habitacionRepository.findAll().stream()
                .filter(h -> estaDisponible(h.getId(), fechaInicio, fechaFin))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Habitacion> obtenerHabitacionesPaginadas(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return habitacionRepository.findByNumeroContainingIgnoreCaseOrTipoContainingIgnoreCase(
                    search, search, pageable);
        }
        return habitacionRepository.findAll(pageable);
    }

    public List<Habitacion> obtenerHabitacionesEnMantenimiento() {
        return habitacionRepository.findAll().stream()
                .filter(h -> "MANTENIMIENTO".equals(h.getEstado()))
                .collect(Collectors.toList());
    }

    // Método sobrecargado para compatibilidad con tests
    public boolean estaDisponible(long habitacionId) {
        return estaDisponible(habitacionId, LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Transactional
    public Habitacion actualizarHabitacion(Habitacion habitacion) {
        if (habitacion.getId() == null) {
            throw new IllegalArgumentException("ID de habitación requerido para actualización");
        }

        Optional<Habitacion> existente = habitacionRepository.findById(habitacion.getId());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Habitación no encontrada");
        }

        // Validar número único si cambió
        if (!existente.get().getNumero().equals(habitacion.getNumero())) {
            if (habitacionRepository.findByNumero(habitacion.getNumero()).isPresent()) {
                throw new IllegalArgumentException("El número de habitación ya existe");
            }
        }

        Habitacion actualizada = habitacionRepository.save(habitacion);
        auditoriaService.registrarAccion("ACTUALIZACION_HABITACION",
                "Habitación actualizada: " + actualizada.getNumero(), "Habitacion", actualizada.getId());
        return actualizada;
    }

    @Transactional
    public void eliminarHabitacion(Long id) {
        Optional<Habitacion> habitacion = habitacionRepository.findById(id);
        if (habitacion.isEmpty()) {
            throw new IllegalArgumentException("Habitación no encontrada");
        }

        // Verificar si tiene reservas activas
        List<Reserva> reservasActivas = reservaRepository.findAll().stream()
                .filter(r -> r.getHabitacion() != null && r.getHabitacion().getId().equals(id))
                .filter(r -> "ACTIVA".equals(r.getEstadoReserva()) || "PENDIENTE".equals(r.getEstadoReserva()))
                .collect(Collectors.toList());

        if (!reservasActivas.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar habitación con reservas activas");
        }

        habitacionRepository.deleteById(id);
        auditoriaService.registrarAccion("ELIMINACION_HABITACION",
                "Habitación eliminada: " + habitacion.get().getNumero(), "Habitacion", id);
    }

    public List<Habitacion> obtenerHabitacionesDisponiblesParaCliente(Long clienteId) {
        // Para clientes, mostrar solo habitaciones disponibles
        return habitacionRepository.findAll().stream()
                .filter(h -> "DISPONIBLE".equals(h.getEstado()))
                .collect(Collectors.toList());
    }

    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return habitacionRepository.findAll().stream()
                .filter(h -> "DISPONIBLE".equals(h.getEstado()))
                .collect(Collectors.toList());
    }
}