package com.gestion.hotelera.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

/**
 * Tests para ReservaService siguiendo TDD
 * 
 * RED-GREEN-REFACTOR:
 * 1. RED: Escribir test que falle
 * 2. GREEN: Escribir código mínimo para que pase
 * 3. REFACTOR: Mejorar el código manteniendo los tests verdes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - Test Driven Development")
@SuppressWarnings("null")
class ReservaServiceTest {

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

    private Cliente cliente;
    private Habitacion habitacion;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(
                reservaRepository,
                auditoriaService,
                servicioRepository,
                habitacionService,
                emailService,
                notificacionService);

        // Datos de prueba
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678");
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");

        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero("101");
        habitacion.setPrecioPorNoche(150.0);

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(LocalDate.now().plusDays(1));
        reserva.setFechaFin(LocalDate.now().plusDays(3));
        reserva.setEstadoReserva("PENDIENTE");
        reserva.setTotalPagar(300.0);
    }

    @Nested
    @DisplayName("Creación de Reservas")
    class CreacionReservas {

        @Test
        @DisplayName("Debería crear una reserva exitosamente")
        void deberiaCrearReservaExitosamente() {
            // Given
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            // When
            Reserva resultado = reservaService.crearOActualizarReserva(reserva);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCliente().getNombres()).isEqualTo("Juan");
            verify(auditoriaService).registrarAccion(
                    eq("CREACION_O_ACTUALIZACION_RESERVA"),
                    contains("Reserva creada o actualizada"),
                    eq("Reserva"),
                    eq(1L));
        }

        @Test
        @DisplayName("Debería registrar auditoría al crear reserva")
        void deberiaRegistrarAuditoriaAlCrearReserva() {
            // Given
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            // When
            reservaService.crearOActualizarReserva(reserva);

            // Then
            verify(auditoriaService).registrarAccion(
                    anyString(),
                    anyString(),
                    eq("Reserva"),
                    eq(1L));
        }
    }

    @Nested
    @DisplayName("Búsqueda de Reservas")
    class BusquedaReservas {

        @Test
        @DisplayName("Debería obtener todas las reservas")
        void deberiaObtenerTodasLasReservas() {
            // Given
            List<Reserva> reservas = Arrays.asList(reserva);
            when(reservaRepository.findAll()).thenReturn(reservas);

            // When
            List<Reserva> resultado = reservaService.obtenerTodasLasReservas();

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debería buscar reserva por ID")
        void deberiaBuscarReservaPorId() {
            // Given
            when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

            // When
            Optional<Reserva> resultado = reservaService.buscarReservaPorId(1L);

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debería retornar vacío cuando no encuentra reserva por ID")
        void deberiaRetornarVacioCuandoNoEncuentraReserva() {
            // Given
            when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Reserva> resultado = reservaService.buscarReservaPorId(999L);

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debería obtener reservas por cliente")
        void deberiaObtenerReservasPorCliente() {
            // Given
            List<Reserva> reservas = Arrays.asList(reserva);
            when(reservaRepository.findByCliente(cliente)).thenReturn(reservas);

            // When
            List<Reserva> resultado = reservaService.obtenerReservasPorCliente(cliente);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getCliente().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Cálculos de Reservas")
    class CalculosReservas {

        @Test
        @DisplayName("Debería calcular días de estadía correctamente")
        void deberiaCalcularDiasEstadia() {
            // Given
            LocalDate inicio = LocalDate.of(2024, 1, 1);
            LocalDate fin = LocalDate.of(2024, 1, 5);

            // When
            Integer dias = reservaService.calcularDiasEstadia(inicio, fin);

            // Then
            assertThat(dias).isEqualTo(4);
        }

        @Test
        @DisplayName("Debería calcular total a pagar correctamente")
        void deberiaCalcularTotalPagar() {
            // Given
            Double precioPorNoche = 150.0;
            Integer dias = 3;

            // When
            Double total = reservaService.calcularTotalPagar(precioPorNoche, dias);

            // Then
            assertThat(total).isEqualTo(450.0);
        }

        @Test
        @DisplayName("Debería calcular ingresos totales de reservas finalizadas")
        void deberiaCalcularIngresosTotales() {
            // Given
            Reserva reserva1 = new Reserva();
            reserva1.setEstadoReserva("FINALIZADA");
            reserva1.setTotalPagar(300.0);

            Reserva reserva2 = new Reserva();
            reserva2.setEstadoReserva("PENDIENTE");
            reserva2.setTotalPagar(200.0);

            Reserva reserva3 = new Reserva();
            reserva3.setEstadoReserva("FINALIZADA");
            reserva3.setTotalPagar(500.0);

            when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva1, reserva2, reserva3));

            // When
            Double ingresos = reservaService.calcularIngresosTotales();

            // Then
            assertThat(ingresos).isEqualTo(800.0);
        }
    }

    @Nested
    @DisplayName("Gestión de Estados")
    class GestionEstados {

        @Test
        @DisplayName("Debería cancelar reserva exitosamente")
        void deberiaCancelarReserva() {
            // Given
            when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            // When
            boolean resultado = reservaService.cancelarReserva(1L);

            // Then
            assertThat(resultado).isTrue();
            verify(auditoriaService).registrarAccion(
                    eq("CANCELACION_RESERVA"),
                    contains("Reserva cancelada"),
                    eq("Reserva"),
                    eq(1L));
        }

        @Test
        @DisplayName("Debería retornar false al cancelar reserva inexistente")
        void deberiaRetornarFalseAlCancelarReservaInexistente() {
            // Given
            when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            boolean resultado = reservaService.cancelarReserva(999L);

            // Then
            assertThat(resultado).isFalse();
            verify(auditoriaService, never()).registrarAccion(anyString(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("Debería finalizar reserva exitosamente")
        void deberiaFinalizarReserva() {
            // Given
            when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            // When
            reservaService.finalizarReserva(1L);

            // Then
            verify(auditoriaService).registrarAccion(
                    eq("FINALIZACION_RESERVA"),
                    contains("Reserva finalizada"),
                    eq("Reserva"),
                    eq(1L));
        }
    }

    @Nested
    @DisplayName("Reportes y Estadísticas")
    class ReportesEstadisticas {

        @Test
        @DisplayName("Debería contar reservas correctamente")
        void deberiaContarReservas() {
            // Given
            when(reservaRepository.count()).thenReturn(5L);

            // When
            long cantidad = reservaService.contarReservas();

            // Then
            assertThat(cantidad).isEqualTo(5L);
        }

        @Test
        @DisplayName("Debería contar reservas por estado")
        void deberiaContarReservasPorEstado() {
            // Given
            when(reservaRepository.countByEstadoReservaIgnoreCase("PENDIENTE")).thenReturn(3L);

            // When
            long cantidad = reservaService.contarReservasPorEstado("PENDIENTE");

            // Then
            assertThat(cantidad).isEqualTo(3L);
        }

        @Test
        @DisplayName("Debería contar check-ins de hoy")
        void deberiaContarCheckInsHoy() {
            // Given
            when(reservaRepository.countByFechaInicio(LocalDate.now())).thenReturn(2L);

            // When
            long cantidad = reservaService.contarCheckInsHoy();

            // Then
            assertThat(cantidad).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debería contar check-outs de hoy")
        void deberiaContarCheckOutsHoy() {
            // Given
            when(reservaRepository.countByFechaFin(LocalDate.now())).thenReturn(1L);

            // When
            long cantidad = reservaService.contarCheckOutsHoy();

            // Then
            assertThat(cantidad).isEqualTo(1L);
        }
    }
}