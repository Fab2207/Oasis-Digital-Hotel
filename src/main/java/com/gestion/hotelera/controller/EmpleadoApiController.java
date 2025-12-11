package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EmpleadoApiController {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Empleado> list() {
        return empleadoRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Empleado getById(@PathVariable long id) {
        return empleadoRepository.findById(id).orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Empleado create(@RequestBody Empleado empleado) {
        
        if (empleado.getUsuario() != null) {
            Usuario u = empleado.getUsuario();
            if (u.getUsername() == null || u.getUsername().isEmpty()) {
                u.setUsername(empleado.getEmail());
            }
            if (u.getPassword() != null) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
            }
            if (u.getRol() == null || u.getRol().isEmpty()) {
                u.setRol("ROLE_RECEPCIONISTA");
            } else if (!u.getRol().startsWith("ROLE_")) {
                u.setRol("ROLE_" + u.getRol());
            }
            u.setActivo(true);
        }
        return empleadoRepository.save(empleado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody Empleado partial) {
        try {
            Empleado existing = empleadoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            existing.setNombres(partial.getNombres());
            existing.setApellidos(partial.getApellidos());
            existing.setEmail(partial.getEmail());
            existing.setTelefono(partial.getTelefono());

            if (partial.getUsuario() != null) {
                Usuario u = existing.getUsuario();
                if (u == null) {
                    
                    u = new Usuario();
                    u.setUsername(partial.getEmail());
                    u.setActivo(true);
                    u.setRol("ROLE_RECEPCIONISTA"); 
                    existing.setUsuario(u);
                }

                if (partial.getUsuario().getRol() != null) {
                    String newRol = partial.getUsuario().getRol();
                    if (!newRol.startsWith("ROLE_"))
                        newRol = "ROLE_" + newRol;
                    u.setRol(newRol);
                }

                if (partial.getUsuario().getPassword() != null && !partial.getUsuario().getPassword().isEmpty()) {
                    u.setPassword(passwordEncoder.encode(partial.getUsuario().getPassword()));
                }
            }

            return ResponseEntity.ok(empleadoRepository.save(existing));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al actualizar empleado: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id) {
        empleadoRepository.deleteById(id);
    }
}
