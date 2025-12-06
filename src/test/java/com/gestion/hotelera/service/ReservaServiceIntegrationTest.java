package com.gestion.hotelera.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ClienteRepository;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tests de integración para ReservaService
 * Prueba la interacción real con la base de datos
 */
@DisplayName("ReservaService - Integration Tests")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReservaServiceIntegrationTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ReservaService reservaService;

    private Cliente cliente;
    private Habitacion habitacion;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba en memoria sin persistir
        cliente = new Cliente();
        cliente.setId(1L);
        String dniAleatorio = String.format("%08d", (int) (Math.random() * 1_0000_0000));
        cliente.setDni(dniAleatorio);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("987654321");

        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero(String.valueOf(System.currentTimeMillis() % 1000000));
        habitacion.setEstado("DISPONIBLE");
        habitacion.setPrecioPorNoche(150.0);
        habitacion.setTipo("Suite");
    }

    @Test
    @DisplayName("Debería crear reserva con persistencia real")
    void deberiaCrearReservaConPersistenciaReal() {
        // Given
        LocalDate inicio = LocalDate.now().plusDays(1);
        LocalDate fin = LocalDate.now().plusDays(3);
        Reserva reserva = crearReserva(inicio, fin, "PENDIENTE", 300.0);
        reserva.setId(1L);

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // When
        Reserva resultado = reservaService.crearOActualizarReserva(reserva);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(resultado.getHabitacion().getId()).isEqualTo(habitacion.getId());
        assertThat(resultado.getEstadoReserva()).isEqualTo("PENDIENTE");

        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("Debería calcular días de estadía correctamente")
    void deberiaCalcularDiasEstadiaCorrectamente() {
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
    void deberiaCalcularTotalPagarCorrectamente() {
        // Given
        Double precioPorNoche = 200.0;
        Integer dias = 5;

        // When
        Double total = reservaService.calcularTotalPagar(precioPorNoche, dias);

        // Then
        assertThat(total).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Debería cancelar reserva y persistir cambio")
    void deberiaCancelarReservaYPersistirCambio() {
        // Given
        Reserva reserva = crearReserva(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "PENDIENTE",
                300.0);
        reserva.setId(1L);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // When
        boolean resultado = reservaService.cancelarReserva(1L);

        // Then
        assertThat(resultado).isTrue();
        verify(reservaRepository).findById(1L);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Debería calcular ingresos totales de reservas finalizadas")
    void deberiaCalcularIngresosTotalesDeReservasFinalizadas() {
        // Given
        Reserva reservaFinalizada1 = crearReserva(
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3),
                "FINALIZADA",
                500.0);
        Reserva reservaFinalizada2 = crearReserva(
                LocalDate.now().minusDays(3),
                LocalDate.now().minusDays(1),
                "FINALIZADA",
                550.0);
        Reserva reservaPendiente = crearReserva(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "PENDIENTE",
                300.0);

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(
                reservaFinalizada1, reservaFinalizada2, reservaPendiente));

        // When
        double ingresos = reservaService.calcularIngresosTotales();

        // Then
        assertThat(ingresos).isEqualTo(1050.0);
        verify(reservaRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener reservas por cliente")
    void deberiaObtenerReservasPorCliente() {
        // Given
        List<Reserva> reservasEsperadas = Arrays.asList(
                crearReserva(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "PENDIENTE", 300.0),
                crearReserva(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), "PENDIENTE", 300.0));

        when(reservaRepository.findByCliente(cliente)).thenReturn(reservasEsperadas);

        // When
        List<Reserva> reservas = reservaService.obtenerReservasPorCliente(cliente);

        // Then
        assertThat(reservas).hasSize(2);
        assertThat(reservas).allMatch(r -> r.getCliente().getId().equals(cliente.getId()));
        verify(reservaRepository).findByCliente(cliente);
    }

    private Reserva crearReserva(LocalDate inicio, LocalDate fin, String estado, double total) {
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(inicio);
        reserva.setFechaFin(fin);
        reserva.setHoraEntrada(LocalTime.of(14, 0));
        reserva.setHoraSalida(LocalTime.of(12, 0));
        reserva.setDiasEstadia((int) ChronoUnit.DAYS.between(inicio, fin));
        reserva.setEstadoReserva(estado);
        reserva.setTotalPagar(total);
        return reserva;
    }
}