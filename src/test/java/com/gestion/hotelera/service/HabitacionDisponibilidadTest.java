package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HabitacionDisponibilidadTest {

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private NotificacionService notificacionService;

    private HabitacionService habitacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        habitacionService = new HabitacionService(habitacionRepository, reservaRepository, auditoriaService,
                notificacionService);
    }

    @Test
    void testObtenerHabitacionesDisponiblesParaCliente_ExcluyeHabitacionesReservadas() {
        // Arrange
        Long clienteId = 1L;

        Habitacion habitacion1 = new Habitacion("101", "Simple", 50.0, "DISPONIBLE");
        habitacion1.setId(1L);

        Habitacion habitacion2 = new Habitacion("102", "Doble", 80.0, "DISPONIBLE");
        habitacion2.setId(2L);

        Habitacion habitacion3 = new Habitacion("103", "Suite", 150.0, "DISPONIBLE");
        habitacion3.setId(3L);

        List<Habitacion> todasDisponibles = Arrays.asList(habitacion1, habitacion2, habitacion3);
        List<Long> habitacionesReservadas = Arrays.asList(2L); // Cliente ya tiene reservada la habitación 2

        when(habitacionRepository.findByEstado("DISPONIBLE")).thenReturn(todasDisponibles);
        when(reservaRepository.findHabitacionesReservadasPorCliente(clienteId)).thenReturn(habitacionesReservadas);

        // Act
        List<Habitacion> resultado = habitacionService.obtenerHabitacionesDisponiblesParaCliente(clienteId);

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(habitacion1));
        assertFalse(resultado.contains(habitacion2)); // Esta debe estar excluida
        assertTrue(resultado.contains(habitacion3));

        verify(habitacionRepository).findByEstado("DISPONIBLE");
        verify(reservaRepository).findHabitacionesReservadasPorCliente(clienteId);
    }

    @Test
    void testObtenerHabitacionesDisponiblesParaCliente_ClienteNulo() {
        // Arrange
        Habitacion habitacion1 = new Habitacion("101", "Simple", 50.0, "DISPONIBLE");
        List<Habitacion> todasDisponibles = Arrays.asList(habitacion1);

        when(habitacionRepository.findByEstado("DISPONIBLE")).thenReturn(todasDisponibles);

        // Act
        List<Habitacion> resultado = habitacionService.obtenerHabitacionesDisponiblesParaCliente(null);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(habitacion1, resultado.get(0));

        verify(habitacionRepository).findByEstado("DISPONIBLE");
        verify(reservaRepository, never()).findHabitacionesReservadasPorCliente(any());
    }

    @Test
    void testObtenerHabitacionesDisponiblesParaCliente_SinReservasActivas() {
        // Arrange
        Long clienteId = 1L;

        Habitacion habitacion1 = new Habitacion("101", "Simple", 50.0, "DISPONIBLE");
        habitacion1.setId(1L);

        List<Habitacion> todasDisponibles = Arrays.asList(habitacion1);
        List<Long> habitacionesReservadas = Arrays.asList(); // Sin reservas activas

        when(habitacionRepository.findByEstado("DISPONIBLE")).thenReturn(todasDisponibles);
        when(reservaRepository.findHabitacionesReservadasPorCliente(clienteId)).thenReturn(habitacionesReservadas);

        // Act
        List<Habitacion> resultado = habitacionService.obtenerHabitacionesDisponiblesParaCliente(clienteId);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(habitacion1, resultado.get(0));

        verify(habitacionRepository).findByEstado("DISPONIBLE");
        verify(reservaRepository).findHabitacionesReservadasPorCliente(clienteId);
    }
}