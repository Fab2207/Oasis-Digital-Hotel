package com.gestion.hotelera.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@oasisdigital.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarConfirmacionReserva(String toEmail, String nombreCliente, String numeroReserva,
            String fechaInicio, String fechaFin, String habitacion,
            Double total) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió confirmación a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Confirmación de Reserva - Oasis Digital");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Su reserva ha sido confirmada exitosamente.\n\n" +
                            "Detalles de la reserva:\n" +
                            "- Número de Reserva: %s\n" +
                            "- Fecha de Entrada: %s\n" +
                            "- Fecha de Salida: %s\n" +
                            "- Habitación: %s\n" +
                            "- Total: S/. %.2f\n\n" +
                            "Gracias por elegir Oasis Digital.\n\n" +
                            "¡Esperamos su visita!",
                    nombreCliente, numeroReserva, fechaInicio, fechaFin, habitacion, total));
            mailSender.send(message);
            log.info("Email de confirmación enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación a: {}", toEmail, e);
        }
    }

    public void enviarNotificacionCheckIn(String toEmail, String nombreCliente, String numeroReserva) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió notificación de check-in a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Check-in Realizado - Oasis Digital");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Su check-in ha sido registrado exitosamente.\n\n" +
                            "Reserva: %s\n\n" +
                            "¡Bienvenido a Oasis Digital! Esperamos que disfrute su estadía.\n\n" +
                            "Si necesita algo, no dude en contactarnos.",
                    nombreCliente, numeroReserva));
            mailSender.send(message);
            log.info("Email de check-in enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de check-in a: {}", toEmail, e);
        }
    }

    public void enviarNotificacionCheckOut(String toEmail, String nombreCliente, String numeroReserva) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió notificación de check-out a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Check-out Realizado - Oasis Digital");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Su check-out ha sido registrado exitosamente.\n\n" +
                            "Reserva: %s\n\n" +
                            "Gracias por elegir Oasis Digital. Esperamos verlo nuevamente pronto.\n\n" +
                            "¡Que tenga un excelente día!",
                    nombreCliente, numeroReserva));
            mailSender.send(message);
            log.info("Email de check-out enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de check-out a: {}", toEmail, e);
        }
    }

    public void enviarNotificacionPago(String toEmail, String nombreCliente, String numeroReserva,
            Double monto, String metodo) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió notificación de pago a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Pago Confirmado - Oasis Digital");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Su pago ha sido procesado exitosamente.\n\n" +
                            "Detalles del pago:\n" +
                            "- Reserva: %s\n" +
                            "- Monto: S/. %.2f\n" +
                            "- Método: %s\n\n" +
                            "Gracias por su pago.",
                    nombreCliente, numeroReserva, monto, metodo));
            mailSender.send(message);
            log.info("Email de pago enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de pago a: {}", toEmail, e);
        }
    }

    /**
     * Envía un email de bienvenida cuando un cliente se registra en el sistema
     */
    public void enviarEmailBienvenida(String toEmail, String nombreCliente, String username) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió bienvenida a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¡Bienvenido a Oasis Digital!");
            message.setText(String.format(
                    "¡Hola %s!\n\n" +
                            "¡Bienvenido a Oasis Digital!\n\n" +
                            "Su cuenta ha sido creada exitosamente. Ahora puede disfrutar de todos nuestros servicios:\n\n"
                            +
                            "- Reservar habitaciones en línea\n" +
                            "- Ver el historial de sus reservas\n" +
                            "- Gestionar sus datos personales\n" +
                            "- Acceder a servicios exclusivos\n\n" +
                            "Datos de su cuenta:\n" +
                            "- Usuario: %s\n" +
                            "- Email: %s\n\n" +
                            "Gracias por elegirnos. ¡Esperamos atenderle pronto!\n\n" +
                            "Atentamente,\n" +
                            "El equipo de Oasis Digital",
                    nombreCliente, username, toEmail));
            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a: {}", toEmail, e);
        }
    }

    /**
     * Envía un recordatorio 24 horas antes del check-in
     */
    public void enviarRecordatorioCheckIn(String toEmail, String nombreCliente, String numeroReserva,
            String fechaCheckIn, String habitacion) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió recordatorio a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recordatorio: Su reserva en Oasis Digital es mañana");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Le recordamos que su reserva en Oasis Digital tiene check-in mañana.\n\n" +
                            "Detalles de su reserva:\n" +
                            "- Número de Reserva: %s\n" +
                            "- Fecha de Check-in: %s\n" +
                            "- Habitación: %s\n" +
                            "- Hora de Check-in: A partir de las 14:00\n\n" +
                            "Importante:\n" +
                            "• Traiga un documento de identidad válido\n" +
                            "• El check-in anticipado está sujeto a disponibilidad\n" +
                            "• Para cualquier consulta, contáctenos\n\n" +
                            "¡Nos vemos pronto!\n\n" +
                            "Atentamente,\n" +
                            "El equipo de Oasis Digital",
                    nombreCliente, numeroReserva, fechaCheckIn, habitacion));
            mailSender.send(message);
            log.info("Email de recordatorio de check-in enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar recordatorio de check-in a: {}", toEmail, e);
        }
    }

    /**
     * Envía una encuesta de satisfacción después del check-out
     */
    public void enviarEncuestaPostEstadia(String toEmail, String nombreCliente, String numeroReserva,
            String fechaCheckOut) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió encuesta a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¿Cómo fue su estadía en Oasis Digital?");
            message.setText(String.format(
                    "Estimado/a %s,\n\n" +
                            "Esperamos que haya disfrutado su estadía en Oasis Digital.\n\n" +
                            "Detalles de su reserva:\n" +
                            "- Número de Reserva: %s\n" +
                            "- Fecha de Check-out: %s\n\n" +
                            "Nos encantaría conocer su opinión sobre nuestros servicios.\n" +
                            "Su feedback es muy importante para nosotros y nos ayuda a mejorar continuamente.\n\n" +
                            "Por favor, califique su experiencia:\n\n" +
                            "• ¿Cómo calificaría la limpieza de su habitación? (1-5 estrellas)\n" +
                            "• ¿Cómo fue la atención del personal? (1-5 estrellas)\n" +
                            "• ¿Recomendaría Oasis Digital a sus amigos/familiares?\n" +
                            "• Comentarios adicionales\n\n" +
                            "Responda a este correo con sus comentarios o contáctenos directamente.\n\n" +
                            "Esperamos volver a verle pronto.\n\n" +
                            "Atentamente,\n" +
                            "El equipo de Oasis Digital",
                    nombreCliente, numeroReserva, fechaCheckOut));
            mailSender.send(message);
            log.info("Email de encuesta post-estadía enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar encuesta post-estadía a: {}", toEmail, e);
        }
    }

    /**
     * Envía un email genérico
     */
    public void enviarEmail(String toEmail, String subject, String content) {
        if (!emailEnabled) {
            log.info("Email deshabilitado. No se envió email genérico a: {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email a: {}", toEmail, e);
        }
    }
}
