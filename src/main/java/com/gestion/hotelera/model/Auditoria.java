package com.gestion.hotelera.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 50)
    private String usuarioUsername;

    @Column(nullable = false, length = 100)
    private String tipoAccion;

    @Column(nullable = false, length = 500)
    private String detalleAccion;

    @Column(length = 50)
    private String entidadAfectada;

    @Column
    private Long entidadAfectadaId;

    public Auditoria() {}
    public Auditoria(LocalDateTime timestamp, String usuarioUsername, String tipoAccion, String detalleAccion, String entidadAfectada, Long entidadAfectadaId) {
        this.timestamp = timestamp; this.usuarioUsername = usuarioUsername; this.tipoAccion = tipoAccion; this.detalleAccion = detalleAccion; this.entidadAfectada = entidadAfectada; this.entidadAfectadaId = entidadAfectadaId;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUsuarioUsername() { return usuarioUsername; }
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }

    public String getTipoAccion() { return tipoAccion; }
    public void setTipoAccion(String tipoAccion) { this.tipoAccion = tipoAccion; }
    public String getDetalleAccion() { return detalleAccion; }
    public void setDetalleAccion(String detalleAccion) { this.detalleAccion = detalleAccion; }
    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }
    public Long getEntidadAfectadaId() { return entidadAfectadaId; }
    public void setEntidadAfectadaId(Long entidadAfectadaId) { this.entidadAfectadaId = entidadAfectadaId; }
}