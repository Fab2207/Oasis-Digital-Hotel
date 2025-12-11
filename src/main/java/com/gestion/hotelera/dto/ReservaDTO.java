package com.gestion.hotelera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservaDTO {
    private Long id;
    private String clienteNombre;
    private String habitacionNumero;
    private String habitacionTipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoReserva;
    private Double total;
    private java.util.List<String> servicios;
    private String metodoPago;
    private Double montoDescuento;
    private String clienteDni;
    private String clienteEmail;
    private Integer diasEstadia;
    private Double habitacionPrecio;
    private LocalDate fechaSalidaReal;
    private ClienteSummary cliente;
    private HabitacionSummary habitacion;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClienteSummary {
        private String nombres;
        private String apellidos;
        private String dni;
        private String email;
        private String telefono;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HabitacionSummary {
        private String numero;
        private String tipo;
        private Double precioPorNoche;
    }
}
