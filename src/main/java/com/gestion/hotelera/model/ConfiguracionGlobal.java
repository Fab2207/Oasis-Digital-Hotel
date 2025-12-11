package com.gestion.hotelera.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "configuracion_global")
public class ConfiguracionGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String clave;

    private String nombreHotel;
    private String colorPrimario; 
    private String colorAcento; 
    private String borderRadius; 

    private String emailContacto;
    private String direccion;
    private String mensajeBienvenida;
    private String urlLogo;

    private boolean emailReserva = true;
    private boolean alertasSeguridad = true;
}
