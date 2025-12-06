package com.gestion.hotelera.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración personalizada para propiedades de email de la aplicación.
 * Estas propiedades personalizadas se usan para configurar el comportamiento
 * del servicio de email.
 */
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailConfigurationProperties {

    /**
     * Dirección de email desde la cual se enviarán todos los correos del sistema
     */
    private String from = "noreply@oasisdigital.com";

    /**
     * Flag para habilitar/deshabilitar el envío de emails
     */
    private boolean enabled = false;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
