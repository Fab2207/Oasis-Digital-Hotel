package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionApiController {

    @Autowired
    private HabitacionService habitacionService;

    /**
     * Obtener habitaciones disponibles por tipo
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<RoomDTO>> obtenerHabitacionesDisponiblesPorTipo(
            @RequestParam("tipo") String tipo) {
        try {
            // Obtener todas las habitaciones del tipo solicitado que estén disponibles
            List<Habitacion> habitaciones = habitacionService.obtenerTodasLasHabitaciones();

            List<RoomDTO> habitacionesDisponibles = habitaciones.stream()
                    .filter(h -> h.getTipo().equalsIgnoreCase(tipo))
                    .filter(h -> "DISPONIBLE".equalsIgnoreCase(h.getEstado()))
                    .map(h -> new RoomDTO(
                            h.getId(),
                            h.getNumero(),
                            h.getTipo(),
                            h.getPrecioPorNoche(),
                            h.getEstado()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(habitacionesDisponibles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO para respuesta JSON
    public static class RoomDTO {
        private Long id;
        private String numero;
        private String tipo;
        private Double precioPorNoche;
        private String estado;

        public RoomDTO(Long id, String numero, String tipo, Double precioPorNoche, String estado) {
            this.id = id;
            this.numero = numero;
            this.tipo = tipo;
            this.precioPorNoche = precioPorNoche;
            this.estado = estado;
        }

        // Getters y Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNumero() {
            return numero;
        }

        public void setNumero(String numero) {
            this.numero = numero;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public Double getPrecioPorNoche() {
            return precioPorNoche;
        }

        public void setPrecioPorNoche(Double precioPorNoche) {
            this.precioPorNoche = precioPorNoche;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
