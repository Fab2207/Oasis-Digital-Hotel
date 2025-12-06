package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByEstado(String estado);

    Optional<Habitacion> findByNumero(String numero);

    long countByEstado(String estado);

    long countByEstadoIgnoreCase(String estado);

    org.springframework.data.domain.Page<Habitacion> findByNumeroContainingIgnoreCaseOrTipoContainingIgnoreCase(
            String numero, String tipo, org.springframework.data.domain.Pageable pageable);
}
