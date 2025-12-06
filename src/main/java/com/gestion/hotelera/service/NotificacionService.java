package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Notificacion;
import com.gestion.hotelera.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findByArchivadaOrderByFechaCreacionDesc(false);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerArchivadas() {
        return notificacionRepository.findByArchivadaOrderByFechaCreacionDesc(true);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNoLeidas() {
        return notificacionRepository.findByLeidaFalseAndArchivadaFalseOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Notificacion> obtenerPorId(Long id) {
        return notificacionRepository.findById(id);
    }

    @Transactional
    public Notificacion crearNotificacion(String titulo, String mensaje, String tipo) {
        Notificacion notificacion = new Notificacion(titulo, mensaje, tipo);
        Notificacion guardada = notificacionRepository.save(notificacion);
        logger.debug("Notificación creada: ID={}, Tipo={}", guardada.getId(), tipo);
        return guardada;
    }

    @Transactional
    public void marcarComoLeida(Long id) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(id);
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
            logger.debug("Notificación marcada como leída: ID={}", id);
        }
    }

    @Transactional
    public void archivar(Long id) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(id);
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setArchivada(true);
            notificacionRepository.save(notificacion);
            logger.debug("Notificación archivada: ID={}", id);
        }
    }

    @Transactional
    public void marcarTodasComoLeidas() {
        List<Notificacion> noLeidas = notificacionRepository
                .findByLeidaFalseAndArchivadaFalseOrderByFechaCreacionDesc();

        if (!noLeidas.isEmpty()) {
            noLeidas.forEach(n -> n.setLeida(true));
            notificacionRepository.saveAll(noLeidas);
            logger.info("Marcadas {} notificaciones como leídas", noLeidas.size());
        }
    }
}
