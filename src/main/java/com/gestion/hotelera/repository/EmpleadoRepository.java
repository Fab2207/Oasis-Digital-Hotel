package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    boolean existsByEmail(String email);
}
