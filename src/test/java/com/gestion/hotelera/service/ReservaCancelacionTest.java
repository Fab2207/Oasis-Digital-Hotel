package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Pago;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class ReservaCancelacionTest {

        @Mock
        private ReservaRepository reservaRepository;

        @Mock
        private AuditoriaService auditoriaService;

        @Mock
        private ServicioRepository servicioRepository;

        @Mock
        private HabitacionService habitacionService;

        @Mock
        private EmailService emailService;

        @Mock
        private NotificacionService notificacionService;

        private ReservaService reservaService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                reservaService = new ReservaService(
                                reservaRepository,
                                auditoriaService,
                                servicioRepository,
                                habitacionService,
                                emailService,
                                notificacionService);
        }

        @Test
        void testCancelarReservaSinPago_DeberiaPermitirCancelacion() {
                // Arrange
                Long reservaId = 1L;
                Cliente cliente = new Cliente();
                cliente.setNombres("Juan");
                cliente.setApellidos("Pérez");
                cliente.setDni("12345678");

                Habitacion habitacion = new Habitacion("101", "Simple", 50.0, "OCUPADA");
                habitacion.setId(1L);

                Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 100.0, "ACTIVA");
                reserva.setId(reservaId);
                // Sin pago asociado
                reserva.setPago(null);

                when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
                when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

                // Act
                boolean resultado = reservaService.cancelarReserva(reservaId);

                // Assert
                assertTrue(resultado);
                assertEquals("CANCELADA", reserva.getEstadoReserva());
                verify(habitacionService).actualizarEstadoHabitacion(1L, "DISPONIBLE");
                verify(auditoriaService).registrarAccion(eq("CANCELACION_RESERVA"), anyString(), eq("Reserva"),
                                eq(reservaId));
        }

        @Test
        void testCancelarReservaConPago_DeberiaLanzarExcepcion() {
                // Arrange
                Long reservaId = 1L;
                Cliente cliente = new Cliente();
                cliente.setNombres("Juan");
                cliente.setApellidos("Pérez");
                cliente.setDni("12345678");

                Habitacion habitacion = new Habitacion("101", "Simple", 50.0, "OCUPADA");

                Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 100.0, "ACTIVA");
                reserva.setId(reservaId);

                // Con pago asociado
                Pago pago = new Pago(reserva, 100.0, 0.0, 100.0, "TARJETA", "COMPLETADO", "REF123", "WEB");
                reserva.setPago(pago);

                when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

                // Act & Assert
                IllegalStateException exception = assertThrows(IllegalStateException.class,
                                () -> reservaService.cancelarReserva(reservaId));

                assertEquals("No se puede cancelar una reserva que ya tiene pago. Use 'Finalizar' en su lugar.",
                                exception.getMessage());

                // Verificar que no se llamaron los métodos de cancelación
                verify(reservaRepository, never()).save(any(Reserva.class));
                verify(habitacionService, never()).actualizarEstadoHabitacion(anyLong(), anyString());
                verify(auditoriaService, never()).registrarAccion(anyString(), anyString(), anyString(), anyLong());
        }

        @Test
        void testFinalizarReservaConPago_DeberiaPermitirFinalizacion() {
                // Arrange
                Long reservaId = 1L;
                Cliente cliente = new Cliente();
                cliente.setNombres("Juan");
                cliente.setApellidos("Pérez");
                cliente.setDni("12345678");

                Habitacion habitacion = new Habitacion("101", "Simple", 50.0, "OCUPADA");
                habitacion.setId(1L);

                Reserva reserva = new Reserva(cliente, habitacion, LocalDate.now(), LocalDate.now().plusDays(2),
                                LocalTime.of(14, 0), LocalTime.of(12, 0), 2, 100.0, "ACTIVA");
                reserva.setId(reservaId);

                // Con pago asociado
                Pago pago = new Pago(reserva, 100.0, 0.0, 100.0, "TARJETA", "COMPLETADO", "REF123", "WEB");
                reserva.setPago(pago);

                when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
                when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

                // Act
                reservaService.finalizarReserva(reservaId);

                // Assert
                assertEquals("FINALIZADA", reserva.getEstadoReserva());
                verify(habitacionService).actualizarEstadoHabitacion(1L, "DISPONIBLE");
                verify(auditoriaService).registrarAccion(eq("FINALIZACION_RESERVA"), anyString(), eq("Reserva"),
                                eq(reservaId));
        }
}