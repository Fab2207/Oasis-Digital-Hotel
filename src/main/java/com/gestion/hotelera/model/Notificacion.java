package com.gestion.hotelera.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private String tipo; // CHECKIN, SOLICITUD, LIMPIEZA, SISTEMA

    @Column(nullable = false)
    private boolean leida = false;

    @Column(nullable = false)
    private boolean archivada = false;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public Notificacion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Notificacion(String titulo, String mensaje, String tipo) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fechaCreacion = LocalDateTime.now();
        this.leida = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public boolean isArchivada() {
        return archivada;
    }

    public void setArchivada(boolean archivada) {
        this.archivada = archivada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // Helper for view
    public String getTiempoRelativo() {
        if (fechaCreacion == null)
            return "";
        long minutes = ChronoUnit.MINUTES.between(fechaCreacion, LocalDateTime.now());
        if (minutes < 60)
            return "Hace " + minutes + " min";
        long hours = ChronoUnit.HOURS.between(fechaCreacion, LocalDateTime.now());
        if (hours < 24)
            return "Hace " + hours + " horas";
        long days = ChronoUnit.DAYS.between(fechaCreacion, LocalDateTime.now());
        return "Hace " + days + " días";
    }
}
