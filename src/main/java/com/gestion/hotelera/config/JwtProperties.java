package com.gestion.hotelera.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expiration;
    private String prefix;
    private String header;

    public String getSecret() { 
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret no puede estar vacío");
        }
        return secret; 
    }
    public void setSecret(String secret) { 
        if (secret != null && secret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret debe tener al menos 32 caracteres");
        }
        this.secret = secret; 
    }
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
}