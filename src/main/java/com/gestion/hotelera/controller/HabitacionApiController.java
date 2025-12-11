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
@CrossOrigin(origins = "http://localhost:4200")
public class HabitacionApiController {

    @Autowired
    private HabitacionService habitacionService;

    @GetMapping
    public ResponseEntity<List<RoomDTO>> obtenerTodas() {
        List<Habitacion> habitaciones = habitacionService.obtenerTodasLasHabitaciones();
        List<RoomDTO> dtos = habitaciones.stream()
                .map(h -> new RoomDTO(
                        h.getId(),
                        h.getNumero(),
                        h.getTipo(),
                        h.getPrecioPorNoche(),
                        h.getEstado()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<RoomDTO>> obtenerHabitacionesDisponiblesPorTipo(
            @RequestParam("tipo") String tipo) {
        try {
            
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

    @GetMapping("/disponibles-fechas")
    public ResponseEntity<List<RoomDTO>> obtenerDisponiblesPorFechas(
            @RequestParam("fechaInicio") String inicioStr,
            @RequestParam("fechaFin") String finStr) {
        try {
            java.time.LocalDate inicio = java.time.LocalDate.parse(inicioStr);
            java.time.LocalDate fin = java.time.LocalDate.parse(finStr);

            List<Habitacion> disponibles = habitacionService.buscarDisponibles(inicio, fin);

            List<RoomDTO> dtos = disponibles.stream().map(this::mapToDTO).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody Habitacion habitacion) {
        try {
            habitacion.setId(null); 
            Habitacion nueva = habitacionService.crearHabitacion(habitacion);
            return ResponseEntity.ok(mapToDTO(nueva));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> actualizar(@PathVariable Long id, @RequestBody Habitacion habitacion) {
        try {
            habitacion.setId(id);
            Habitacion actualizada = habitacionService.actualizarHabitacion(habitacion);
            return ResponseEntity.ok(mapToDTO(actualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            habitacionService.eliminarHabitacion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private RoomDTO mapToDTO(Habitacion h) {
        return new RoomDTO(
                h.getId(),
                h.getNumero(),
                h.getTipo(),
                h.getPrecioPorNoche(),
                h.getEstado());
    }

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
