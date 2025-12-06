package com.gestion.hotelera.dto;

public class PagoResponse {
    private boolean exito;
    private String mensaje;
    private String referencia;
    private String estado;
    private Double montoBase;
    private Double montoServicios;
    private Double montoTotal;
    private Long reservaId;

    public PagoResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public PagoResponse(String estado, String mensaje, String referencia, Double montoBase, Double montoServicios, Double montoTotal, Long reservaId) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.referencia = referencia;
        this.montoBase = montoBase;
        this.montoServicios = montoServicios;
        this.montoTotal = montoTotal;
        this.reservaId = reservaId;
    }

    // Getters and setters
    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Double getMontoBase() { return montoBase; }
    public void setMontoBase(Double montoBase) { this.montoBase = montoBase; }
    public Double getMontoServicios() { return montoServicios; }
    public void setMontoServicios(Double montoServicios) { this.montoServicios = montoServicios; }
    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
}
