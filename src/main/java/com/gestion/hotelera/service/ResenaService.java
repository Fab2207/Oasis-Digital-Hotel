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
    private final com.gestion.hotelera.repository.ClienteRepository clienteRepository; 

    public ResenaService(ResenaRepository resenaRepository,
            com.gestion.hotelera.repository.ClienteRepository clienteRepository) {
        this.resenaRepository = resenaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Resena guardarResena(Resena resena, String username) {
        
        if (username != null) {
            com.gestion.hotelera.model.Cliente cliente = clienteRepository.findByEmail(username)
                    .orElseGet(() -> clienteRepository.findByUsuarioUsername(username).orElse(null));
            if (cliente != null) {
                resena.setCliente(cliente);
            }
        }
        return guardarResena(resena);
    }

    @Transactional
    public Resena guardarResena(Resena resena) {
        
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
    public List<Resena> obtenerPorUsuario(String username) {
        com.gestion.hotelera.model.Cliente cliente = clienteRepository.findByEmail(username)
                .orElseGet(() -> clienteRepository.findByUsuarioUsername(username).orElse(null));
        if (cliente == null)
            return java.util.Collections.emptyList();
        return resenaRepository.findByClienteId(cliente.getId());
    }

    @Transactional(readOnly = true)
    public boolean existeResenaParaReserva(Long reservaId) {
        return !resenaRepository.findByReservaId(reservaId).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<Resena> obtenerResenasPendientes() {
        return resenaRepository.findByAprobada(false);
    }

    public List<Resena> obtenerAprobadas() {
        return obtenerResenasAprobadas();
    }

    @Transactional(readOnly = true)
    public List<Resena> obtenerResenasAprobadas() {
        return resenaRepository.findByAprobada(true);
    }

    @Transactional
    public Resena aprobarResena(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));
        resena.setAprobada(true);
        Resena saved = resenaRepository.save(resena);
        logger.info("Reseña aprobada: ID={}", resenaId);
        return saved;
    }

    @Transactional
    public void rechazarResena(Long resenaId) {
        resenaRepository.deleteById(resenaId);
        logger.info("Reseña rechazada y eliminada: ID={}", resenaId);
    }

    @Transactional(readOnly = true)
    public List<Resena> obtenerTodas() {
        return resenaRepository.findAll();
    }

    @Transactional
    public Resena responderResena(Long resenaId, String respuesta) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));
        resena.setRespuesta(respuesta);
        resena.setFechaRespuesta(LocalDateTime.now());
        
        if (!Boolean.TRUE.equals(resena.getAprobada())) {
            resena.setAprobada(true);
        }
        return resenaRepository.save(resena);
    }
}
