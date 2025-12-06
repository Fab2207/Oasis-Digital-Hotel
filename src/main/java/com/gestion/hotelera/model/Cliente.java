package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "clientes")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank
    @Size(min = 8, max = 8)
    @Pattern(regexp = "^[0-9]{8}$")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s-]*$")
    @Column(length = 50)
    private String nacionalidad;

    @Email
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Transient
    private boolean hasActiveReservations;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @Transient
    private java.time.LocalDate ultimaEstancia;

    @Transient
    private Long totalReservas;

    public Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        if (nombres != null && nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres no pueden estar vacíos");
        }
        this.nombres = nombres != null ? nombres.trim() : null;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        if (apellidos != null && apellidos.trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos");
        }
        this.apellidos = apellidos != null ? apellidos.trim() : null;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isHasActiveReservations() {
        return hasActiveReservations;
    }

    public void setHasActiveReservations(boolean hasActiveReservations) {
        this.hasActiveReservations = hasActiveReservations;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public java.time.LocalDate getUltimaEstancia() {
        return ultimaEstancia;
    }

    public void setUltimaEstancia(java.time.LocalDate ultimaEstancia) {
        this.ultimaEstancia = ultimaEstancia;
    }

    public Long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(Long totalReservas) {
        this.totalReservas = totalReservas;
    }
}