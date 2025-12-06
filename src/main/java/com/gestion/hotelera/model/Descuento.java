package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "descuentos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String descripcion;

    @NotNull(message = "El tipo de descuento es obligatorio")
    @Column(nullable = false, length = 20)
    private String tipo; // PORCENTAJE o MONTO_FIJO

    @NotNull(message = "El valor del descuento es obligatorio")
    @Positive(message = "El valor debe ser positivo")
    @Column(nullable = false)
    private Double valor;

    @Column(name = "monto_minimo")
    private Double montoMinimo;

    @Column(name = "monto_maximo_descuento")
    private Double montoMaximoDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "usos_maximos")
    private Integer usosMaximos;

    @Column(name = "usos_actuales")
    private Integer usosActuales = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "descuento", fetch = FetchType.LAZY)
    private Set<Reserva> reservas = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (usosActuales == null) {
            usosActuales = 0;
        }
    }

    public Descuento() {}

    public boolean esValido() {
        LocalDate hoy = LocalDate.now();
        return activo && 
               !hoy.isBefore(fechaInicio) && 
               !hoy.isAfter(fechaFin) &&
               (usosMaximos == null || usosActuales < usosMaximos);
    }

    public Double calcularDescuento(Double montoBase) {
        if (!esValido() || montoBase == null || montoBase <= 0) {
            return 0.0;
        }

        if (montoMinimo != null && montoBase < montoMinimo) {
            return 0.0;
        }

        Double descuento = 0.0;
        if ("PORCENTAJE".equalsIgnoreCase(tipo)) {
            descuento = montoBase * (valor / 100.0);
            if (montoMaximoDescuento != null && descuento > montoMaximoDescuento) {
                descuento = montoMaximoDescuento;
            }
        } else if ("MONTO_FIJO".equalsIgnoreCase(tipo)) {
            descuento = Math.min(valor, montoBase);
        }

        return descuento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo != null ? codigo.toUpperCase().trim() : null; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public Double getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(Double montoMinimo) { this.montoMinimo = montoMinimo; }
    public Double getMontoMaximoDescuento() { return montoMaximoDescuento; }
    public void setMontoMaximoDescuento(Double montoMaximoDescuento) { this.montoMaximoDescuento = montoMaximoDescuento; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Integer getUsosMaximos() { return usosMaximos; }
    public void setUsosMaximos(Integer usosMaximos) { this.usosMaximos = usosMaximos; }
    public Integer getUsosActuales() { return usosActuales; }
    public void setUsosActuales(Integer usosActuales) { this.usosActuales = usosActuales; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Set<Reserva> getReservas() { return reservas; }
    public void setReservas(Set<Reserva> reservas) { this.reservas = reservas; }
}

