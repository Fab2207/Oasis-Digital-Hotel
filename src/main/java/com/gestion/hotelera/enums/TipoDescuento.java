package com.gestion.hotelera.enums;

/**
 * Tipos de descuento disponibles en el sistema.
 */
public enum TipoDescuento {
    PORCENTAJE("PORCENTAJE"),
    MONTO_FIJO("MONTO_FIJO");

    private final String valor;

    TipoDescuento(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoDescuento fromString(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        for (TipoDescuento tipo : TipoDescuento.values()) {
            if (tipo.valor.equalsIgnoreCase(texto.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de descuento no válido: " + texto);
    }
}
