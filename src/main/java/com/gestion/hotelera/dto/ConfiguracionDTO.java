package com.gestion.hotelera.dto;

public class ConfiguracionDTO {
    private String nombreHotel;
    private String emailContacto;
    private String direccion;
    private String idioma;
    private String zonaHoraria;

    private String telefono;
    private String moneda;

    public ConfiguracionDTO() {
        // Valores por defecto
        this.nombreHotel = "Oasis Digital Resort";
        this.emailContacto = "contacto@oasisdigital.com";
        this.direccion = "Av. del Sol 123, Cancún, México";
        this.idioma = "es";
        this.zonaHoraria = "America/Mexico_City";
        this.telefono = "+52 998 123 4567";
        this.moneda = "USD";
    }

    public String getNombreHotel() {
        return nombreHotel;
    }

    public void setNombreHotel(String nombreHotel) {
        this.nombreHotel = nombreHotel;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
