package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Pago;
import com.gestion.hotelera.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReserva(Reserva reserva);
    Optional<Pago> findByReservaId(Long reservaId);
    boolean existsByReserva(Reserva reserva);
}

