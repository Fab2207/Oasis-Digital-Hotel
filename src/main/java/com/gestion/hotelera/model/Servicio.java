package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "servicios")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombre;

    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;

    @ManyToMany(mappedBy = "servicios")
    @JsonIgnore
    private Set<Reserva> reservas = new HashSet<>();

    @ElementCollection
    private List<String> opciones = new ArrayList<>();

    public Servicio() {
    }

    public Servicio(String nombre, String descripcion, Double precio, Boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(Set<Reserva> reservas) {
        this.reservas = reservas;
    }

    public List<String> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<String> opciones) {
        this.opciones = opciones;
    }
}
