
package com.gestion.hotelera.config;

import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class TestDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final HabitacionService habitacionService;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(UsuarioRepository usuarioRepository, 
                              HabitacionService habitacionService, 
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.habitacionService = habitacionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Crear usuarios básicos para tests
            crearUsuarioSiNoExiste("admin", "admin123", "ROLE_ADMIN");
            crearUsuarioSiNoExiste("recepcionista", "recep123", "ROLE_RECEPCIONISTA");
            crearUsuarioSiNoExiste("cliente", "cliente123", "ROLE_CLIENTE");
            
            // Inicializar habitaciones
            habitacionService.inicializarHabitacionesSiNoExisten();
        } catch (Exception e) {
            System.err.println("Error inicializando datos de test: " + e.getMessage());
        }
    }

    private void crearUsuarioSiNoExiste(String username, String rawPassword, String rol) {
        try {
            if (usuarioRepository.findByUsername(username).isEmpty()) {
                Usuario usuario = new Usuario(username, passwordEncoder.encode(rawPassword), rol);
                usuarioRepository.save(usuario);
            }
        } catch (Exception e) {
            System.err.println("Error creando usuario de test " + username + ": " + e.getMessage());
        }
    }
}