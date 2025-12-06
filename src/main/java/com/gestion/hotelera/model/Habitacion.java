package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "habitaciones")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false)
    private Double precioPorNoche;

    @Column(nullable = false, length = 20)
    private String estado;

    public Habitacion() {}
    public Habitacion(Long id) { this.id = id; }
    public Habitacion(String numero, String tipo, Double precioPorNoche, String estado) {
        this.numero = numero; this.tipo = tipo; this.precioPorNoche = precioPorNoche; this.estado = estado;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(Double precioPorNoche) { this.precioPorNoche = precioPorNoche; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}