package com.gestion.hotelera.service;

import com.gestion.hotelera.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("El nombre de usuario no puede estar vacío");
        }
        try {
            return usuarioRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error al buscar usuario");
        }
    }
}