package com.gestion.hotelera.exception;

import java.time.LocalDate;
import java.util.List;

public class ClienteConReservasActivasException extends RuntimeException {

    private final Long clienteId;
    private final List<ReservaActivaResumen> reservasActivas;

    public ClienteConReservasActivasException(Long clienteId, List<ReservaActivaResumen> reservasActivas) {
        super("No puedes eliminar a este cliente porque tiene una reserva activa. Cancela o finaliza la reserva para continuar.");
        this.clienteId = clienteId;
        this.reservasActivas = reservasActivas;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public List<ReservaActivaResumen> getReservasActivas() {
        return reservasActivas;
    }

    public record ReservaActivaResumen(
            Long id,
            String habitacion,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
    }
}
