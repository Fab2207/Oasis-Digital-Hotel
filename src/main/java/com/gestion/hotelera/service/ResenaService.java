package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Resena;
import com.gestion.hotelera.repository.ResenaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@SuppressWarnings("null")
public class ResenaService {

    private static final Logger logger = LoggerFactory.getLogger(ResenaService.class);

    private final ResenaRepository resenaRepository;

    public ResenaService(ResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public Resena guardarResena(Resena resena) {
        // Las nuevas reseñas quedan pendientes de aprobación
        resena.setAprobada(false);
        resena.setFechaCreacion(LocalDateTime.now());

        Resena guardada = resenaRepository.save(resena);
        logger.info("Reseña guardada pendiente de aprobación: ID={}, Cliente ID={}",
                guardada.getId(),
                guardada.getCliente() != null ? guardada.getCliente().getId() : "N/A");
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Resena> obtenerPorCliente(Long clienteId) {
        return resenaRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public boolean existeResenaParaReserva(Long reservaId) {
        return !resenaRepository.findByReservaId(reservaId).isEmpty();
    }

    // Obtener reseñas pendientes de aprobación (para ADMIN)
    @Transactional(readOnly = true)
    public List<Resena> obtenerResenasPendientes() {
        return resenaRepository.findByAprobada(false);
    }

    // Obtener reseñas aprobadas (públicas)
    @Transactional(readOnly = true)
    public List<Resena> obtenerResenasAprobadas() {
        return resenaRepository.findByAprobada(true);
    }

    // Aprobar reseña (ADMIN)
    @Transactional
    public void aprobarResena(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));
        resena.setAprobada(true);
        resenaRepository.save(resena);
        logger.info("Reseña aprobada: ID={}", resenaId);
    }

    // Rechazar/eliminar reseña (ADMIN)
    @Transactional
    public void rechazarResena(Long resenaId) {
        resenaRepository.deleteById(resenaId);
        logger.info("Reseña rechazada y eliminada: ID={}", resenaId);
    }
}
