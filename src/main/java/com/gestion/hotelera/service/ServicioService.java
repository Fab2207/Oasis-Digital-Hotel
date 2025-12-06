package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final AuditoriaService auditoriaService;

    public ServicioService(ServicioRepository servicioRepository, AuditoriaService auditoriaService) {
        this.servicioRepository = servicioRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }

    public Optional<Servicio> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try {
            return servicioRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Servicio guardar(Servicio servicio) {
        Objects.requireNonNull(servicio, "El servicio a guardar no puede ser nulo");
        Servicio guardado = servicioRepository.save(servicio);
        auditoriaService.registrarAccion("SERVICIO_GUARDADO",
                "Servicio '" + guardado.getNombre() + "' actualizado/creado.",
                "Servicio",
                guardado.getId());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de servicio inválido");
        }
        try {
            if (servicioRepository.existsById(id)) {
                servicioRepository.deleteById(id);
                auditoriaService.registrarAccion("SERVICIO_ELIMINADO",
                        "Servicio eliminado (ID: " + id + ")",
                        "Servicio",
                        id);
            } else {
                throw new IllegalArgumentException("Servicio no encontrado");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar servicio: " + e.getMessage(), e);
        }
    }

    public List<Servicio> buscarPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return servicioRepository.findAllById(ids);
    }

    // Métodos alias para compatibilidad con ServicioController
    public List<Servicio> obtenerTodosLosServicios() {
        return listarTodos();
    }

    @Transactional
    public void crearServicio(Servicio servicio) {
        guardar(servicio);
    }

    @Transactional
    public void actualizarServicio(Servicio servicio) {
        guardar(servicio);
    }

    @Transactional
    public void eliminarServicio(Long id) {
        eliminar(id);
    }

    public Optional<Servicio> obtenerServicioPorId(Long id) {
        return buscarPorId(id);
    }
}
