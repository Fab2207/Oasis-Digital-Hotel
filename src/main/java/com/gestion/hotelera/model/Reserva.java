package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "reservas")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private LocalTime horaEntrada;

    @Column(nullable = false)
    private LocalTime horaSalida;

    @Column(nullable = false)
    private Integer diasEstadia;

    @Column(nullable = false)
    private Double totalPagar;

    @Column(nullable = false, length = 20)
    private String estadoReserva;

    @Column(name = "fecha_salida_real")
    private LocalDate fechaSalidaReal;

    @Column(name = "fecha_checkin_real")
    private LocalDate fechaCheckinReal;

    @Column(name = "hora_checkin_real")
    private LocalTime horaCheckinReal;

    @Column(name = "hora_checkout_real")
    private LocalTime horaCheckoutReal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descuento_id")
    private Descuento descuento;

    @Column(name = "monto_descuento")
    private Double montoDescuento = 0.0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "reserva_servicios", joinColumns = @JoinColumn(name = "reserva_id"), inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    private Set<Servicio> servicios = new HashSet<>();

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pago pago;

    @ElementCollection
    @CollectionTable(name = "reserva_opciones_servicio", joinColumns = @JoinColumn(name = "reserva_id"))
    @MapKeyColumn(name = "servicio_nombre")
    @Column(name = "opcion_seleccionada")
    private Map<String, String> opcionesServicios = new HashMap<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resena> resenas = new HashSet<>();

    public Reserva(Cliente cliente, Habitacion habitacion, LocalDate fechaInicio, LocalDate fechaFin,
            LocalTime horaEntrada, LocalTime horaSalida, Integer diasEstadia, Double totalPagar, String estadoReserva) {
        this.cliente = cliente;
        this.habitacion = habitacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.diasEstadia = diasEstadia;
        this.totalPagar = totalPagar;
        this.estadoReserva = estadoReserva;
    }

    public Reserva() {
    }

    public double calcularTotalServicios() {
        if (servicios == null || servicios.isEmpty())
            return 0.0;
        return servicios.stream().mapToDouble(servicio -> servicio.getPrecio() != null ? servicio.getPrecio() : 0.0)
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.fechaFin = fechaFin;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Integer getDiasEstadia() {
        return diasEstadia;
    }

    public void setDiasEstadia(Integer diasEstadia) {
        this.diasEstadia = diasEstadia;
    }

    public Double getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(Double totalPagar) {
        if (totalPagar != null && totalPagar < 0) {
            throw new IllegalArgumentException("El total a pagar no puede ser negativo");
        }
        this.totalPagar = totalPagar;
    }

    public String getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public LocalDate getFechaSalidaReal() {
        return fechaSalidaReal;
    }

    public void setFechaSalidaReal(LocalDate fechaSalidaReal) {
        this.fechaSalidaReal = fechaSalidaReal;
    }

    public Set<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(Set<Servicio> servicios) {
        this.servicios = servicios;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Map<String, String> getOpcionesServicios() {
        return opcionesServicios;
    }

    public void setOpcionesServicios(Map<String, String> opcionesServicios) {
        this.opcionesServicios = opcionesServicios;
    }

    public LocalDate getFechaCheckinReal() {
        return fechaCheckinReal;
    }

    public void setFechaCheckinReal(LocalDate fechaCheckinReal) {
        this.fechaCheckinReal = fechaCheckinReal;
    }

    public LocalTime getHoraCheckinReal() {
        return horaCheckinReal;
    }

    public void setHoraCheckinReal(LocalTime horaCheckinReal) {
        this.horaCheckinReal = horaCheckinReal;
    }

    public LocalTime getHoraCheckoutReal() {
        return horaCheckoutReal;
    }

    public void setHoraCheckoutReal(LocalTime horaCheckoutReal) {
        this.horaCheckoutReal = horaCheckoutReal;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
    }

    public Double getMontoDescuento() {
        return montoDescuento;
    }

    public void setMontoDescuento(Double montoDescuento) {
        this.montoDescuento = montoDescuento != null ? montoDescuento : 0.0;
    }

    public Set<Resena> getResenas() {
        return resenas;
    }

    public void setResenas(Set<Resena> resenas) {
        this.resenas = resenas;
    }

    public Double calcularTotalConDescuento() {
        double total = (totalPagar != null ? totalPagar : 0.0) + calcularTotalServicios();
        return Math.max(0.0, total - (montoDescuento != null ? montoDescuento : 0.0));
    }
}
