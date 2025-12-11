package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.security.JwtService;
import com.gestion.hotelera.service.ClienteService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository; 
    private final JwtService jwtService;
    private final ClienteService clienteService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Usuario user = usuarioRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();
        String token = jwtService.getToken(user);

        String displayName = user.getUsername();
        if ("ROLE_CLIENTE".equals(user.getRol()) && user.getCliente() != null) {
            displayName = user.getCliente().getNombres();
        } else if (user.getEmpleado() != null) {
            displayName = user.getEmpleado().getNombres();
        }

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(request.getUsername()) 
                .role(user.getRol())
                .displayName(displayName) 
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build(); 
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword()); 
        usuario.setRol("ROLE_CLIENTE");
        usuario.setActivo(true);

        Cliente cliente = new Cliente();
        cliente.setNombres(request.getNombres());
        cliente.setApellidos(request.getApellidos());
        cliente.setDni(request.getDni()); 
        cliente.setTelefono(request.getTelefono()); 
        cliente.setEmail(request.getEmail());
        cliente.setUsuario(usuario);

        clienteService.crearCliente(cliente);

        Usuario createdUser = usuarioRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.getToken(createdUser);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .role("ROLE_CLIENTE")
                .displayName(request.getNombres())
                .build());
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String password;
        private String nombres;
        private String apellidos;
        private String email;
        private String dni;
        private String telefono;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private String displayName;
    }
}
