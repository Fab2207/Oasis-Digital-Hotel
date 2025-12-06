package com.gestion.hotelera.service;

import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.enums.EstadoReserva;
import com.gestion.hotelera.model.Pago;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.PagoRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);
    private static final String METODO_DEFAULT = "TARJETA";
    private static final String CANAL_DEFAULT = "WEB";
    private static final String ESTADO_COMPLETADO = "COMPLETADO";

    private final PagoRepository pagoRepository;
    private final ReservaService reservaService;
    private final ReservaRepository reservaRepository;
    private final EmailService emailService;
    private final com.gestion.hotelera.service.NotificacionService notificacionService;

    public PagoService(PagoRepository pagoRepository,
            ReservaService reservaService,
            ReservaRepository reservaRepository,
            EmailService emailService,
            com.gestion.hotelera.service.NotificacionService notificacionService) {
        this.pagoRepository = pagoRepository;
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
        this.emailService = emailService;
        this.notificacionService = notificacionService;
    }

    @Transactional(rollbackFor = Exception.class)
    public PagoResponse procesarPago(PagoRequest pagoRequest) {
        validarPagoRequest(pagoRequest);

        logger.info("Iniciando procesamiento de pago - Reserva ID: {}, Método: {}",
                pagoRequest.getReservaId(), pagoRequest.getMetodoPago());

        String metodo = normalizarMetodoPago(pagoRequest);
        Reserva reserva = obtenerReserva(pagoRequest.getReservaId());

        // Verificar si ya existe un pago
        Optional<Pago> pagoExistente = verificarPagoExistente(reserva.getId());
        if (pagoExistente.isPresent()) {
            return crearRespuestaPagoExistente(pagoExistente.get());
        }

        // Calcular montos
        MontoPago montoPago = calcularMontos(reserva);
        logger.debug("Cálculo de pago - Base: {}, Servicios: {}, Descuento: {}, Total: {}",
                montoPago.base, montoPago.servicios, montoPago.descuento, montoPago.total);

        // Crear y guardar pago
        Pago pago = crearPago(reserva, montoPago, metodo, pagoRequest.getCanal());
        Pago pagoGuardado = guardarPago(pago);

        // Actualizar reserva
        actualizarReservaConPago(reserva, pagoGuardado);

        // Enviar notificación por email
        enviarNotificacionPago(reserva, montoPago.total, metodo);
        
        // Crear notificación en el sistema
        crearNotificacionPago(reserva, montoPago.total, metodo);

        logger.info("Pago procesado exitosamente - Pago ID: {}, Referencia: {}",
                pagoGuardado.getId(), pagoGuardado.getReferencia());

        return crearRespuestaExitosa(pagoGuardado);
    }

    @Transactional(readOnly = true)
    public Optional<Pago> obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }

    // ============== MÉTODOS PRIVADOS DE AYUDA ==============

    private void validarPagoRequest(PagoRequest pagoRequest) {
        if (pagoRequest == null) {
            logger.error("PagoRequest es null");
            throw new IllegalArgumentException("Datos de pago inválidos");
        }
        if (pagoRequest.getReservaId() == null || pagoRequest.getReservaId() <= 0) {
            throw new IllegalArgumentException("ID de reserva inválido");
        }
    }

    private String normalizarMetodoPago(PagoRequest pagoRequest) {
        String metodo = pagoRequest.getMetodo();
        if (metodo == null || metodo.trim().isEmpty()) {
            metodo = pagoRequest.getMetodoPago();
        }
        if (metodo == null || metodo.trim().isEmpty()) {
            metodo = METODO_DEFAULT;
        }

        // Actualizar ambos campos para consistencia
        pagoRequest.setMetodo(metodo);
        pagoRequest.setMetodoPago(metodo);

        if (metodo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar un método de pago");
        }

        return metodo;
    }

    private Reserva obtenerReserva(Long reservaId) {
        return reservaService.obtenerReservaPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    private Optional<Pago> verificarPagoExistente(Long reservaId) {
        Optional<Pago> pagoExistente = pagoRepository.findByReservaId(reservaId);
        if (pagoExistente.isPresent()) {
            logger.info("Ya existe un pago para esta reserva. ID: {}", pagoExistente.get().getId());
        }
        return pagoExistente;
    }

    private PagoResponse crearRespuestaPagoExistente(Pago pago) {
        PagoResponse response = new PagoResponse(true, "El pago ya fue procesado anteriormente.");
        response.setReferencia(pago.getReferencia());
        return response;
    }

    private MontoPago calcularMontos(Reserva reserva) {
        double base = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
        double servicios = reserva.calcularTotalServicios();
        double descuento = reserva.getMontoDescuento() != null ? reserva.getMontoDescuento() : 0.0;
        double total = Math.max(0.0, (base + servicios) - descuento);

        return new MontoPago(base, servicios, descuento, total);
    }

    private Pago crearPago(Reserva reserva, MontoPago montoPago, String metodo, String canal) {
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMontoBase(montoPago.base);
        pago.setMontoServicios(montoPago.servicios);
        pago.setMontoDescuento(montoPago.descuento);
        pago.setMontoTotal(montoPago.total);
        pago.setMetodo(metodo);
        pago.setEstado(ESTADO_COMPLETADO);
        pago.setReferencia(generarReferencia(reserva.getId()));
        pago.setFechaPago(LocalDateTime.now());
        pago.setCanal(canal != null && !canal.trim().isEmpty() ? canal : CANAL_DEFAULT);

        return pago;
    }

    private String generarReferencia(Long reservaId) {
        return "REF-" + System.currentTimeMillis() + "-" + reservaId;
    }

    private Pago guardarPago(Pago pago) {
        logger.debug("Guardando pago para reserva ID: {}", pago.getReserva().getId());
        Pago pagoGuardado = pagoRepository.save(pago);

        if (pagoGuardado.getId() == null) {
            throw new RuntimeException("Error al guardar el pago: el ID no se generó correctamente");
        }

        logger.debug("Pago guardado exitosamente - ID: {}, Referencia: {}",
                pagoGuardado.getId(), pagoGuardado.getReferencia());

        return pagoGuardado;
    }

    private void actualizarReservaConPago(Reserva reserva, Pago pago) {
        reserva.setPago(pago);
        reserva.setEstadoReserva(EstadoReserva.ACTIVA.getValor());
        reservaRepository.save(reserva);
        logger.debug("Reserva actualizada con pago - Reserva ID: {}", reserva.getId());
    }

    private void enviarNotificacionPago(Reserva reserva, double montoTotal, String metodo) {
        if (emailService != null && reserva.getCliente() != null) {
            String email = reserva.getCliente().getEmail();
            if (email != null && !email.trim().isEmpty()) {
                String nombre = reserva.getCliente().getNombres();
                String numeroReserva = String.valueOf(reserva.getId());
                emailService.enviarNotificacionPago(email, nombre, numeroReserva, montoTotal, metodo);
                logger.debug("Notificación de pago enviada a: {}", email);
            }
        }
    }

    private void crearNotificacionPago(Reserva reserva, double montoTotal, String metodo) {
        if (notificacionService != null && reserva.getCliente() != null) {
            String nombreCliente = reserva.getCliente().getNombres() + " " + reserva.getCliente().getApellidos();
            String titulo = "Pago Procesado - Reserva #" + reserva.getId();
            String mensaje = String.format("Pago de S/. %.2f procesado exitosamente para Reserva #%d. Cliente: %s, Método: %s",
                    montoTotal,
                    reserva.getId(),
                    nombreCliente,
                    metodo);
            notificacionService.crearNotificacion(titulo, mensaje, "SISTEMA");
            logger.debug("Notificación de pago creada en sistema: Reserva ID={}", reserva.getId());
        }
    }

    private PagoResponse crearRespuestaExitosa(Pago pago) {
        PagoResponse response = new PagoResponse(true, "Pago procesado exitosamente.");
        response.setReferencia(pago.getReferencia());
        return response;
    }

    // Clase interna para encapsular los montos calculados
    private static class MontoPago {
        final double base;
        final double servicios;
        final double descuento;
        final double total;

        MontoPago(double base, double servicios, double descuento, double total) {
            this.base = base;
            this.servicios = servicios;
            this.descuento = descuento;
            this.total = total;
        }
    }
}
