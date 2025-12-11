package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class UsuarioApiController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> toggleEstado(@PathVariable long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            
            if ("admin".equalsIgnoreCase(usuario.getUsername())) {
                return ResponseEntity.badRequest().body(usuario);
            }
            usuario.setActivo(!usuario.getActivo());
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }
}
