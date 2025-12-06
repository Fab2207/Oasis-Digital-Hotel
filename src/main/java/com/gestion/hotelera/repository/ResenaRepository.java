package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByClienteId(Long clienteId);

    List<Resena> findByReservaId(Long reservaId);

    // Para moderación
    List<Resena> findByAprobada(Boolean aprobada);
}
