package com.gestion.hotelera.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String nombres;
    private String apellidos;
    private String dni;
    private String nacionalidad;
    private String email;
    private String telefono;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.isEmpty() &&
                nombres != null && !nombres.trim().isEmpty() &&
                apellidos != null && !apellidos.trim().isEmpty() &&
                dni != null && dni.matches("\\d{8}") &&
                email != null && email.contains("@");
    }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}