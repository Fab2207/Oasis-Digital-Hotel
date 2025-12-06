package com.gestion.hotelera.dto;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {}
    public LoginRequest(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username.trim();
        } else {
            this.username = null;
        }
    }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.isEmpty();
    }
}