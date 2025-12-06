package com.gestion.hotelera.dto;

public class PagoRequest {
    private Long reservaId;
    private String metodoPago;
    private String metodo;
    private String canal;
    private String numeroTarjeta;
    private String fechaExpiracion;
    private String cvv;
    private String numeroTelefono;
    private DatosTarjeta tarjeta;
    private DatosWallet wallet;

    public static class DatosTarjeta {
        private String numero;
        private String cvv;
        private String fechaExp;
        private String titular;

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        public String getFechaExp() { return fechaExp; }
        public void setFechaExp(String fechaExp) { this.fechaExp = fechaExp; }
        public String getTitular() { return titular; }
        public void setTitular(String titular) { this.titular = titular; }
    }

    public static class DatosWallet {
        private String telefono;
        private String titular;

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getTitular() { return titular; }
        public void setTitular(String titular) { this.titular = titular; }
    }

    // Getters and setters
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) {
        if (reservaId != null && reservaId <= 0) {
            throw new IllegalArgumentException("ID de reserva debe ser positivo");
        }
        this.reservaId = reservaId;
    }

    public boolean isValid() {
        // Validar que el ID de reserva sea válido
        if (reservaId == null || reservaId <= 0) {
            return false;
        }

        // Validar que al menos uno de los campos de método de pago esté presente
        boolean tieneMetodoPago = metodoPago != null && !metodoPago.trim().isEmpty();
        boolean tieneMetodo = metodo != null && !metodo.trim().isEmpty();

        return tieneMetodoPago || tieneMetodo;
    }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
    public String getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(String fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }
    public DatosTarjeta getTarjeta() { return tarjeta; }
    public void setTarjeta(DatosTarjeta tarjeta) { this.tarjeta = tarjeta; }
    public DatosWallet getWallet() { return wallet; }
    public void setWallet(DatosWallet wallet) { this.wallet = wallet; }
}
