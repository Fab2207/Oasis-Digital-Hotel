package com.gestion.hotelera.service;

import com.gestion.hotelera.dto.AuthResponse;
import com.gestion.hotelera.dto.LoginRequest;
import com.gestion.hotelera.dto.RegisterRequest;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ClienteService clienteService;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, ClienteService clienteService,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.clienteService = clienteService;
        this.emailService = emailService;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Datos de login inválidos");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            Usuario user = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String token = jwtService.getToken(user);

            return new AuthResponse(token);
        } catch (Exception e) {
            throw new RuntimeException("Error en autenticación: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Datos de registro inválidos");
        }

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        Usuario user = new Usuario();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRol("ROLE_CLIENTE");

        Usuario userGuardado = usuarioRepository.save(user);

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDni(request.getDni());
        nuevoCliente.setNombres(request.getNombres());
        nuevoCliente.setApellidos(request.getApellidos());
        nuevoCliente.setNacionalidad(request.getNacionalidad());
        nuevoCliente.setEmail(request.getEmail());
        nuevoCliente.setTelefono(request.getTelefono());
        nuevoCliente.setUsuario(userGuardado);

        clienteService.guardar(nuevoCliente);

        // Enviar email de bienvenida al nuevo cliente
        if (emailService != null && nuevoCliente.getEmail() != null && !nuevoCliente.getEmail().trim().isEmpty()) {
            String to = nuevoCliente.getEmail();
            String nombre = nuevoCliente.getNombres();
            String username = userGuardado.getUsername();
            emailService.enviarEmailBienvenida(to, nombre, username);
        }

        String token = jwtService.getToken(userGuardado);

        return new AuthResponse(token);
    }
}
