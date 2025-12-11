package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.ReservaDTO;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ReservaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.model.Cliente;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ReservaApiController {

    private final ReservaService reservaService;
    private final ClienteService clienteService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReservaApiController.class);

    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaDTO>> getMisReservas(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Solicitando reservas para usuario: {}", username);

        Cliente cliente = clienteService.obtenerPorUsername(username);
        if (cliente == null) {
            logger.warn("No se encontró perfil de Cliente para el usuario: {}", username);
            return ResponseEntity.ok(List.of());
        }

        List<Reserva> reservas = reservaService.obtenerReservasPorCliente(cliente);
        logger.info("Se encontraron {} reservas para el cliente ID: {}", reservas.size(), cliente.getId());

        return ResponseEntity.ok(mapToDTOs(reservas));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ReservaDTO>> getRecent(@RequestParam(defaultValue = "5") int limit) {
        List<Reserva> reservas = reservaService.obtenerUltimasReservas(limit);
        return ResponseEntity.ok(mapToDTOs(reservas));
    }

    @PostMapping("/{id}/checkin")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> checkIn(@PathVariable Long id) {
        reservaService.realizarCheckIn(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/checkout")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> checkOut(@PathVariable Long id) {
        reservaService.realizarCheckOut(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancelar")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        if (reservaService.cancelarReserva(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> getById(@PathVariable Long id) {
        return reservaService.buscarReservaPorId(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> getAll() {
        List<Reserva> reservas = reservaService.obtenerTodasLasReservas();
        reservas.sort((r1, r2) -> r2.getId().compareTo(r1.getId())); 
        return ResponseEntity.ok(mapToDTOs(reservas));
    }

    @PostMapping
    public ResponseEntity<ReservaDTO> create(@RequestBody Reserva reserva) {
        
        if (reserva.getEstadoReserva() == null) {
            reserva.setEstadoReserva("PENDIENTE");
        }
        
        if (reserva.getDiasEstadia() == null && reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(reserva.getFechaInicio(), reserva.getFechaFin());
            reserva.setDiasEstadia(dias <= 0 ? 1 : (int) dias);
        }

        Reserva saved = reservaService.crearOActualizarReserva(reserva);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @PostMapping("/{id}/servicios")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> asignarServicios(@PathVariable Long id, @RequestBody List<Long> serviciosIds) {
        
        List<Integer> cantidades = serviciosIds.stream().map(s -> 1).collect(Collectors.toList());
        reservaService.asignarServicios(id, serviciosIds, cantidades);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            if (reservaService.eliminarReservaFisica(id)) {
                return ResponseEntity.noContent().build();
            }
        } catch (IllegalStateException e) {
            
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ReservaDTO mapToDTO(Reserva r) {
        return ReservaDTO.builder()
                .id(r.getId())
                .clienteNombre(
                        r.getCliente() != null ? r.getCliente().getNombres() + " " + r.getCliente().getApellidos()
                                : "Cliente Eliminado")
                .habitacionNumero(r.getHabitacion() != null ? r.getHabitacion().getNumero() : "N/A")
                .habitacionTipo(r.getHabitacion() != null ? r.getHabitacion().getTipo() : "")
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .estadoReserva(r.getEstadoReserva())
                
                .total(r.calcularTotalConDescuento())
                .servicios(r.getServicios().stream().map(s -> s.getNombre() + " - S/." + s.getPrecio())
                        .collect(Collectors.toList()))
                .metodoPago(r.getPago() != null ? r.getPago().getMetodo()
                        : (r.getEstadoReserva().equals("PENDIENTE") ? "Por definir" : "En efectivo"))
                .montoDescuento(r.getMontoDescuento())
                .clienteDni(r.getCliente() != null ? r.getCliente().getDni() : "")
                .clienteEmail(r.getCliente() != null ? r.getCliente().getEmail() : "")
                .diasEstadia(r.getDiasEstadia())
                .habitacionPrecio(r.getHabitacion() != null && r.getHabitacion().getPrecioPorNoche() != null
                        ? r.getHabitacion().getPrecioPorNoche()
                        : 0.0)
                .fechaSalidaReal(r.getFechaSalidaReal())
                .cliente(r.getCliente() != null ? ReservaDTO.ClienteSummary.builder()
                        .nombres(r.getCliente().getNombres())
                        .apellidos(r.getCliente().getApellidos())
                        .dni(r.getCliente().getDni())
                        .email(r.getCliente().getEmail())
                        .telefono(r.getCliente().getTelefono())
                        .build() : null)
                .habitacion(r.getHabitacion() != null ? ReservaDTO.HabitacionSummary.builder()
                        .numero(r.getHabitacion().getNumero())
                        .tipo(r.getHabitacion().getTipo())
                        .precioPorNoche(r.getHabitacion().getPrecioPorNoche())
                        .build() : null)
                .build();
    }

    private List<ReservaDTO> mapToDTOs(List<Reserva> reservas) {
        return reservas.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
