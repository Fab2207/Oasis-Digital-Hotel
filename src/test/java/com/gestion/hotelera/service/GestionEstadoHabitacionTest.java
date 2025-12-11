package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GestionEstadoHabitacionTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private com.gestion.hotelera.repository.ClienteRepository clienteRepository;

    private ReservaService reservaService;
    private HabitacionService habitacionService;

    @BeforeEach
    void setUp() {
        habitacionService = new HabitacionService(
                habitacionRepository,
                reservaRepository,
                auditoriaService,
                notificacionService);
        reservaService = new ReservaService(
                reservaRepository,
                auditoriaService,
                servicioRepository,
                habitacionService,
                emailService,
                notificacionService,
                clienteRepository);
    }

    @Test
    void deberiaOcuparHabitacionAlCrearReservaActiva() {
        // Arrange
        Habitacion habitacion = new Habitacion("101", "Doble", 80.0, "DISPONIBLE");
        habitacion.setId(1L);
        Cliente cliente = new Cliente();
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("123456789");

        Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 160.0, "ACTIVA");
        reserva.setId(1L);

        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        reservaService.crearOActualizarReserva(reserva);

        // Assert
        verify(habitacionRepository).save(argThat(h -> "OCUPADA".equals(h.getEstado())));
    }

    @Test
    void deberiaLiberarHabitacionAlCancelarReserva() {
        // Arrange
        Habitacion habitacion = new Habitacion("101", "Doble", 80.0, "OCUPADA");
        habitacion.setId(1L);
        Cliente cliente = new Cliente();
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("123456789");

        Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 160.0, "ACTIVA");
        reserva.setId(1L);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        boolean resultado = reservaService.cancelarReserva(1L);

        // Assert
        assertTrue(resultado);
        verify(habitacionRepository).save(argThat(h -> "DISPONIBLE".equals(h.getEstado())));
    }

    @Test
    void deberiaLiberarHabitacionAlFinalizarReserva() {
        // Arrange
        Habitacion habitacion = new Habitacion("101", "Doble", 80.0, "OCUPADA");
        habitacion.setId(1L);
        Cliente cliente = new Cliente();
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("123456789");

        Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 160.0, "ACTIVA");
        reserva.setId(1L);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        reservaService.finalizarReserva(1L);

        // Assert
        verify(habitacionRepository).save(argThat(h -> "DISPONIBLE".equals(h.getEstado())));
    }

    @Test
    void deberiaRechazarReservaEnHabitacionNoDisponible() {
        // Arrange
        Habitacion habitacion = new Habitacion("101", "Doble", 80.0, "OCUPADA");
        habitacion.setId(1L);
        Cliente cliente = new Cliente();
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("123456789");

        Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 160.0, "ACTIVA");

        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservaService.crearOActualizarReserva(reserva));

        assertEquals("La habitación seleccionada no está disponible", exception.getMessage());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void deberiaVerificarDisponibilidadCorrectamente() {
        // Arrange
        Habitacion disponible = new Habitacion("101", "Doble", 80.0, "DISPONIBLE");
        Habitacion ocupada = new Habitacion("102", "Simple", 50.0, "OCUPADA");

        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(disponible));
        when(habitacionRepository.findById(2L)).thenReturn(Optional.of(ocupada));

        // Act & Assert
        assertTrue(habitacionService.estaDisponible(1L));
        assertFalse(habitacionService.estaDisponible(2L));
        assertFalse(habitacionService.estaDisponible(999L)); // ID inexistente
    }
}