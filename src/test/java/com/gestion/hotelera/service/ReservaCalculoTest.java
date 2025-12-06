package com.gestion.hotelera.service;

import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReservaCalculoTest {

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
    void testCalcularDiasEstadia_MismoDia_DeberiaRetornar1() {
        // Arrange
        LocalDate fecha = LocalDate.of(2023, 11, 17);

        // Act
        Integer dias = reservaService.calcularDiasEstadia(fecha, fecha);

        // Assert
        assertEquals(1, dias, "Una reserva del mismo día debe contar como 1 día");
    }

    @Test
    void testCalcularDiasEstadia_DosDias_DeberiaRetornar2() {
        // Arrange
        LocalDate inicio = LocalDate.of(2023, 11, 17);
        LocalDate fin = LocalDate.of(2023, 11, 19);

        // Act
        Integer dias = reservaService.calcularDiasEstadia(inicio, fin);

        // Assert
        assertEquals(2, dias, "Una reserva de 2 días debe contar como 2 días");
    }

    @Test
    void testCalcularTotalPagar_ReservaMismoDia() {
        // Arrange
        Double precioPorNoche = 100.0;
        Integer dias = 1; // Mismo día

        // Act
        Double total = reservaService.calcularTotalPagar(precioPorNoche, dias);

        // Assert
        assertEquals(100.0, total, "El total para 1 día debe ser el precio por noche");
    }

    @Test
    void testCalcularTotalPagar_ReservaVariosDias() {
        // Arrange
        Double precioPorNoche = 150.0;
        Integer dias = 3;

        // Act
        Double total = reservaService.calcularTotalPagar(precioPorNoche, dias);

        // Assert
        assertEquals(450.0, total, "El total para 3 días debe ser 3 veces el precio por noche");
    }
}