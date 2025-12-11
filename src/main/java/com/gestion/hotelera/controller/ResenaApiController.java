package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Resena;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ResenaRepository;
import com.gestion.hotelera.service.AuditoriaService;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.NotificacionService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resenas")
public class ResenaApiController {

    private final ResenaRepository resenaRepository;
    private final ReservaService reservaService;
    private final ClienteService clienteService;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public ResenaApiController(ResenaRepository resenaRepository, ReservaService reservaService,
            ClienteService clienteService, AuditoriaService auditoriaService, NotificacionService notificacionService) {
        this.resenaRepository = resenaRepository;
        this.reservaService = reservaService;
        this.clienteService = clienteService;
        this.auditoriaService = auditoriaService;
        this.notificacionService = notificacionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> crearResena(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            String username = auth.getName();
            Cliente cliente = clienteService.obtenerPorUsername(username);

            Map<String, Object> reservaMap = (Map<String, Object>) payload.get("reserva");
            if (reservaMap == null || reservaMap.get("id") == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "ID de reserva faltante"));
            }

            Long reservaId = Long.parseLong(String.valueOf(reservaMap.get("id")));
            Integer calificacion = Integer.parseInt(String.valueOf(payload.get("calificacion")));
            String comentario = (String) payload.get("comentario");

            Optional<Reserva> reservaOpt = reservaService.buscarReservaPorId(reservaId);

            if (reservaOpt.isEmpty() || !reservaOpt.get().getCliente().getId().equals(cliente.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Reserva inválida o no pertenece al cliente."));
            }

            Reserva reserva = reservaOpt.get();
            if (!"FINALIZADA".equals(reserva.getEstadoReserva()) && !"CANCELADA".equals(reserva.getEstadoReserva())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Solo puedes reseñar estancias finalizadas o canceladas."));
            }

            if (!resenaRepository.findByReservaId(reservaId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Ya has enviado una reseña para esta estancia."));
            }

            Resena resena = new Resena();
            resena.setCliente(cliente);
            resena.setReserva(reserva);
            resena.setCalificacion(calificacion);
            resena.setComentario(comentario);
            resena.setAprobada(false);

            resenaRepository.save(resena);
            auditoriaService.registrarAccion("CREACION_RESENA", "Cliente envió reseña ID: " + resena.getId(), "Resena",
                    resena.getId());

            notificacionService.crearNotificacion(
                    "Nueva Reseña Pendiente",
                    "El cliente " + cliente.getNombres() + " envió una reseña para la habitación "
                            + reserva.getHabitacion().getNumero(),
                    "SOLICITUD",
                    "STAFF");

            return ResponseEntity.ok(Map.of("message", "Reseña enviada correctamente."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al procesar reseña: " + e.getMessage()));
        }
    }

    @GetMapping("/todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<java.util.List<Map<String, Object>>> obtenerTodasLasResenas() {
        java.util.List<Resena> resenas = resenaRepository.findAll();
        
        resenas.sort((a, b) -> b.getId().compareTo(a.getId()));

        java.util.List<Map<String, Object>> result = resenas.stream().map(r -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getId());
            map.put("calificacion", r.getCalificacion());
            map.put("comentario", r.getComentario());
            map.put("aprobada", r.getAprobada());
            map.put("respuesta", r.getRespuesta());
            map.put("fechaCreacion", r.getFechaCreacion());
            map.put("fechaRespuesta", r.getFechaRespuesta());

            if (r.getCliente() != null) {
                map.put("clienteNombre", r.getCliente().getNombres() + " " + r.getCliente().getApellidos());
            }
            if (r.getReserva() != null && r.getReserva().getHabitacion() != null) {
                map.put("habitacionNumero", r.getReserva().getHabitacion().getNumero());
            }

            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> aprobarResena(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return resenaRepository.findById(id).map(r -> {
            r.setAprobada(true);
            resenaRepository.save(r);
            auditoriaService.registrarAccion("APROBACION_RESENA", "Reseña aprobada ID: " + id, "Resena", id);
            return ResponseEntity.ok(Map.of("message", "Reseña aprobada correctamente."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rechazarResena(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        if (resenaRepository.existsById(id)) {
            resenaRepository.deleteById(id);
            auditoriaService.registrarAccion("ELIMINACION_RESENA", "Reseña rechazada/eliminada ID: " + id, "Resena",
                    id);
            return ResponseEntity.ok(Map.of("message", "Reseña eliminada correctamente."));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> responderResena(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        String respuesta = payload.get("respuesta");
        if (respuesta == null || respuesta.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La respuesta no puede estar vacía."));
        }

        return resenaRepository.findById(id).map(r -> {
            r.setRespuesta(respuesta);
            r.setFechaRespuesta(java.time.LocalDateTime.now());
            if (!r.getAprobada()) {
                r.setAprobada(true); 
            }
            resenaRepository.save(r);
            auditoriaService.registrarAccion("RESPUESTA_RESENA", "Respuesta enviada para reseña ID: " + id, "Resena",
                    id);

            if (r.getCliente() != null && r.getCliente().getUsuario() != null) {
                notificacionService.crearNotificacion(
                        "Respuesta a tu Reseña",
                        "El administrador ha respondido a tu reseña.",
                        "PERSONAL",
                        r.getCliente().getUsuario().getUsername());
            }

            return ResponseEntity.ok(Map.of("message", "Respuesta enviada correctamente."));
        }).orElse(ResponseEntity.notFound().build());
    }
}
