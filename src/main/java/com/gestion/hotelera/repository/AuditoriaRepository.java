package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findByUsuarioUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<Auditoria> findByTipoAccionContainingIgnoreCaseOrDetalleAccionContainingIgnoreCase(String tipoAccion,
            String detalleAccion, Pageable pageable);

    org.springframework.data.domain.Page<Auditoria> findByTimestampBetween(java.time.LocalDateTime start,
            java.time.LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Auditoria> findByTipoAccionContainingIgnoreCaseAndTimestampBetween(
            String tipoAccion, java.time.LocalDateTime start, java.time.LocalDateTime end,
            org.springframework.data.domain.Pageable pageable);
}