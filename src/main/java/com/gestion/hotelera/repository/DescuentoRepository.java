package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {
       Optional<Descuento> findByCodigo(String codigo);

       List<Descuento> findByActivoTrue();

       @Query("SELECT d FROM Descuento d WHERE d.activo = true " +
                     "AND d.fechaInicio <= :hoy AND d.fechaFin >= :hoy " +
                     "AND (d.usosMaximos IS NULL OR d.usosActuales < d.usosMaximos)")
       List<Descuento> findDescuentosValidos(@Param("hoy") LocalDate hoy);

       @Query("SELECT d FROM Descuento d WHERE d.activo = true " +
                     "AND d.fechaInicio <= :hoy AND d.fechaFin >= :hoy")
       List<Descuento> findDescuentosActivos(@Param("hoy") LocalDate hoy);

       org.springframework.data.domain.Page<Descuento> findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                     String codigo, String descripcion, org.springframework.data.domain.Pageable pageable);
}
