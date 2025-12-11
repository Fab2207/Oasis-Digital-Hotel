package com.gestion.hotelera.enums;

public enum EstadoReserva {
    PENDIENTE("PENDIENTE"),
    ACTIVA("ACTIVA"),
    FINALIZADA("FINALIZADA"),
    CANCELADA("CANCELADA"),
    ARCHIVADA("ARCHIVADA");

    private final String valor;

    EstadoReserva(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoReserva fromString(String valor) {
        for (EstadoReserva estado : EstadoReserva.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de reserva no válido: " + valor);
    }
}