package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class HabitacionServiceTest {

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private HabitacionService habitacionService;

    @BeforeEach
    void setUp() {
        habitacionService = new HabitacionService(
                habitacionRepository,
                reservaRepository,
                auditoriaService);
    }

    @Test
    void testRegistrarHabitacion() {
        Habitacion habitacion = new Habitacion();
        habitacion.setNumero("201");
        habitacion.setEstado("DISPONIBLE");
        habitacion.setPrecioPorNoche(150.0);
        habitacion.setTipo("Suite");

        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(habitacion);

        Habitacion guardada = habitacionService.crearHabitacion(habitacion);

        assertThat(guardada).isNotNull();
        assertThat(guardada.getNumero()).isEqualTo("201");
    }

    @Test
    void testListarHabitaciones() {
        Habitacion h1 = new Habitacion();
        h1.setNumero("101");
        h1.setEstado("DISPONIBLE");

        Habitacion h2 = new Habitacion();
        h2.setNumero("102");
        h2.setEstado("OCUPADA");

        when(habitacionRepository.findAll()).thenReturn(List.of(h1, h2));

        List<Habitacion> habitaciones = habitacionService.obtenerTodasLasHabitaciones();

        assertThat(habitaciones).hasSize(2);
        assertThat(habitaciones.get(0).getNumero()).isEqualTo("101");
    }

    @Test
    void testListarPorEstado() {
        Habitacion h1 = new Habitacion();
        h1.setNumero("301");
        h1.setEstado("MANTENIMIENTO");

        when(habitacionRepository.findByEstado("MANTENIMIENTO")).thenReturn(List.of(h1));

        List<Habitacion> mantenimiento = habitacionService.obtenerHabitacionesEnMantenimiento();

        assertThat(mantenimiento).isNotEmpty();
        assertThat(mantenimiento.get(0).getNumero()).isEqualTo("301");
    }
}