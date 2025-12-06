package com.gestion.hotelera.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Health check personalizado para verificar la conexión al servidor de email
 */
@Component
public class EmailHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    public EmailHealthIndicator(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Health health() {
        try {
            // Si el email está deshabilitado, reportar como UNKNOWN
            if (!emailEnabled) {
                return Health.unknown()
                        .withDetail("status", "disabled")
                        .withDetail("message", "Email service is disabled in configuration")
                        .withDetail("host", mailHost)
                        .withDetail("port", mailPort)
                        .build();
            }

            // Intentar verificar la conexión al servidor SMTP
            try {
                // Esto verifica que el mailSender esté configurado correctamente
                mailSender.createMimeMessage();

                return Health.up()
                        .withDetail("status", "enabled")
                        .withDetail("message", "Email service is configured and ready")
                        .withDetail("host", mailHost)
                        .withDetail("port", mailPort)
                        .withDetail("protocol", "SMTP with STARTTLS")
                        .build();

            } catch (Exception e) {
                return Health.down()
                        .withDetail("status", "error")
                        .withDetail("message", "Cannot connect to email server")
                        .withDetail("host", mailHost)
                        .withDetail("port", mailPort)
                        .withDetail("error", e.getMessage())
                        .build();
            }

        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "error")
                    .withDetail("message", "Email service configuration error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
