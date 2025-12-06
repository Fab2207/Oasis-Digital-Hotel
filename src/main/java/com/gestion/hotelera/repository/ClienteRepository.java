package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByUsuarioUsername(String username);

    Page<Cliente> findByDniContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String dni, String nombres, String apellidos, Pageable pageable);
}
