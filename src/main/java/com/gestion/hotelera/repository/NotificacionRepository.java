package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByLeidaFalseAndArchivadaFalseOrderByFechaCreacionDesc();

    List<Notificacion> findByArchivadaOrderByFechaCreacionDesc(boolean archivada);

    List<Notificacion> findAllByOrderByFechaCreacionDesc();
}
