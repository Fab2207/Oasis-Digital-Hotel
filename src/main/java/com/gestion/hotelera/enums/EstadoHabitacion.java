package com.gestion.hotelera.enums;

public enum EstadoHabitacion {
    DISPONIBLE("DISPONIBLE"),
    OCUPADA("OCUPADA"),
    LIMPIEZA("LIMPIEZA"),
    MANTENIMIENTO("MANTENIMIENTO");

    private final String valor;

    EstadoHabitacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoHabitacion fromString(String valor) {
        for (EstadoHabitacion estado : EstadoHabitacion.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de habitación no válido: " + valor);
    }
}