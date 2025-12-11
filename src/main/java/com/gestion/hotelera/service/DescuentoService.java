package com.gestion.hotelera.service;

import com.gestion.hotelera.enums.TipoDescuento;
import com.gestion.hotelera.model.Descuento;
import com.gestion.hotelera.repository.DescuentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class DescuentoService {

    private static final Logger logger = LoggerFactory.getLogger(DescuentoService.class);
    
    private final DescuentoRepository descuentoRepository;

    public DescuentoService(DescuentoRepository descuentoRepository) {
        this.descuentoRepository = descuentoRepository;
    }

    @Transactional
    public Descuento crearDescuento(Descuento descuento) {
        validarDescuento(descuento);
        validarCodigoUnico(descuento.getCodigo(), null);

        descuento.setCodigo(normalizarCodigo(descuento.getCodigo()));
        Descuento nuevoDescuento = descuentoRepository.save(descuento);
        logger.info("Descuento creado: ID={}, Código={}", nuevoDescuento.getId(), nuevoDescuento.getCodigo());
        return nuevoDescuento;
    }

    @Transactional
    public Descuento actualizarDescuento(Descuento descuento) {
        if (descuento == null || descuento.getId() == null) {
            throw new IllegalArgumentException("El descuento y su ID son obligatorios");
        }

        Descuento existente = descuentoRepository.findById(descuento.getId())
                .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado"));

        if (!existente.getCodigo().equalsIgnoreCase(descuento.getCodigo())) {
            validarCodigoUnico(descuento.getCodigo(), existente.getId());
        }

        actualizarDatos(existente, descuento);
        Descuento actualizado = descuentoRepository.save(existente);
        logger.info("Descuento actualizado: ID={}, Código={}", actualizado.getId(), actualizado.getCodigo());
        return actualizado;
    }

    @Transactional(readOnly = true)
    public Optional<Descuento> obtenerPorId(Long id) {
        return descuentoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Descuento> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        return descuentoRepository.findByCodigo(normalizarCodigo(codigo));
    }

    @Transactional(readOnly = true)
    public Optional<Descuento> validarYBuscarDescuento(String codigo, Double montoBase) {
        Optional<Descuento> descuentoOpt = buscarPorCodigo(codigo);
        if (descuentoOpt.isEmpty()) {
            logger.debug("Descuento no encontrado: código={}", codigo);
            return Optional.empty();
        }

        Descuento descuento = descuentoOpt.get();
        if (!descuento.esValido()) {
            logger.debug("Descuento no válido o expirado: código={}", codigo);
            return Optional.empty();
        }

        if (descuento.getMontoMinimo() != null && montoBase < descuento.getMontoMinimo()) {
            logger.debug("Monto base ({}) menor al mínimo requerido ({})", montoBase, descuento.getMontoMinimo());
            return Optional.empty();
        }

        return descuentoOpt;
    }

    @Transactional
    public void incrementarUso(Descuento descuento) {
        if (descuento != null && descuento.getUsosActuales() != null) {
            descuento.setUsosActuales(descuento.getUsosActuales() + 1);
            descuentoRepository.save(descuento);
            logger.debug("Incrementado uso del descuento: ID={}, Usos={}", descuento.getId(),
                    descuento.getUsosActuales());
        }
    }

    @Transactional(readOnly = true)
    public List<Descuento> obtenerDescuentosValidos() {
        return descuentoRepository.findDescuentosValidos(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Descuento> obtenerTodosLosDescuentos() {
        return descuentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Descuento> obtenerDescuentosActivos() {
        return descuentoRepository.findDescuentosActivos(LocalDate.now());
    }

    @Transactional
    public void eliminarDescuento(Long id) {
        descuentoRepository.deleteById(id);
        logger.info("Descuento eliminado: ID={}", id);
    }

    @Transactional(readOnly = true)
    public Page<Descuento> obtenerDescuentosPaginados(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return descuentoRepository.findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                    search, search, pageable);
        }
        return descuentoRepository.findAll(pageable);
    }

    private void validarDescuento(Descuento descuento) {
        if (descuento == null) {
            throw new IllegalArgumentException("El descuento no puede ser nulo");
        }
        if (descuento.getCodigo() == null || descuento.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del descuento es obligatorio");
        }
        if (descuento.getFechaInicio() == null || descuento.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (descuento.getFechaInicio().isAfter(descuento.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        validarTipoDescuento(descuento.getTipo());
    }

    private void validarTipoDescuento(String tipo) {
        try {
            TipoDescuento.fromString(tipo);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El tipo de descuento debe ser PORCENTAJE o MONTO_FIJO");
        }
    }

    private void validarCodigoUnico(String codigo, Long idExcluir) {
        Optional<Descuento> existente = descuentoRepository.findByCodigo(normalizarCodigo(codigo));
        if (existente.isPresent() && (idExcluir == null || !existente.get().getId().equals(idExcluir))) {
            throw new IllegalArgumentException("Ya existe un descuento con ese código");
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo.toUpperCase().trim();
    }

    private void actualizarDatos(Descuento existente, Descuento nuevo) {
        existente.setCodigo(normalizarCodigo(nuevo.getCodigo()));
        existente.setDescripcion(nuevo.getDescripcion());
        existente.setTipo(nuevo.getTipo());
        existente.setValor(nuevo.getValor());
        existente.setMontoMinimo(nuevo.getMontoMinimo());
        existente.setMontoMaximoDescuento(nuevo.getMontoMaximoDescuento());
        existente.setFechaInicio(nuevo.getFechaInicio());
        existente.setFechaFin(nuevo.getFechaFin());
        existente.setUsosMaximos(nuevo.getUsosMaximos());
        existente.setActivo(nuevo.getActivo());
    }
}
