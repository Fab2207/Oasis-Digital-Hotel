package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "pagos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "monto_base", nullable = false)
    private Double montoBase;

    @Column(name = "monto_servicios", nullable = false)
    private Double montoServicios;

    @Column(name = "monto_descuento", nullable = false)
    private Double montoDescuento = 0.0;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(nullable = false, length = 20)
    private String metodo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 60)
    private String referencia;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(length = 30)
    private String canal;

    public Pago() {}
    public Pago(Reserva reserva, Double montoBase, Double montoServicios, Double montoTotal, String metodo, String estado, String referencia, String canal) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (montoTotal == null || montoTotal < 0) {
            throw new IllegalArgumentException("El monto total debe ser mayor o igual a 0");
        }
        this.reserva = reserva;
        this.montoBase = montoBase != null ? montoBase : 0.0;
        this.montoServicios = montoServicios != null ? montoServicios : 0.0;
        this.montoTotal = montoTotal;
        this.metodo = metodo;
        this.estado = estado;
        this.referencia = referencia;
        this.fechaPago = LocalDateTime.now();
        this.canal = canal;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    public Double getMontoBase() { return montoBase; }
    public void setMontoBase(Double montoBase) { this.montoBase = montoBase; }
    public Double getMontoServicios() { return montoServicios; }
    public void setMontoServicios(Double montoServicios) { this.montoServicios = montoServicios; }
    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public Double getMontoDescuento() { return montoDescuento; }
    public void setMontoDescuento(Double montoDescuento) { this.montoDescuento = montoDescuento != null ? montoDescuento : 0.0; }
}

