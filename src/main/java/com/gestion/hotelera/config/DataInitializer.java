package com.gestion.hotelera.config;

import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.ServicioRepository;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.service.AuditoriaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

        @Bean
        CommandLineRunner initDatabase(ServicioRepository servicioRepository, UsuarioRepository usuarioRepository,
                        com.gestion.hotelera.repository.HabitacionRepository habitacionRepository,
                        PasswordEncoder passwordEncoder,
                        AuditoriaService auditoriaService) {
                return args -> {
                        
                        crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, "admin", "admin123", "ROLE_ADMIN",
                                        auditoriaService);
                        crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, "recep", "recep123",
                                        "ROLE_RECEPCIONISTA", auditoriaService);
                        crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, "cliente", "cliente123",
                                        "ROLE_CLIENTE", auditoriaService);

                        if (habitacionRepository.count() == 0) {
                                System.out.println("🛏️ Inicializando habitaciones...");

                                crearHabitacion(habitacionRepository, "101", "Simple", 50.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "102", "Simple", 50.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "103", "Simple", 50.0, auditoriaService);

                                crearHabitacion(habitacionRepository, "201", "Doble", 80.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "202", "Doble", 80.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "203", "Doble", 80.0, auditoriaService);

                                crearHabitacion(habitacionRepository, "301", "Suite Junior", 120.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "302", "Suite Junior", 120.0, auditoriaService);

                                crearHabitacion(habitacionRepository, "401", "Suite Familiar", 150.0, auditoriaService);
                                crearHabitacion(habitacionRepository, "402", "Suite Familiar", 150.0, auditoriaService);

                                System.out.println("✅ 10 habitaciones creadas exitosamente");
                        }

                        System.out.println("📦 Verificando servicios del hotel...");

                        crearServicioSiNoExiste(servicioRepository, "Spa y Masajes",
                                        "Tratamientos relajantes con aceites esenciales y terapias especializadas",
                                        120.00, auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Gimnasio 24/7",
                                        "Equipamiento de última generación disponible las 24 horas del día", 0.00,
                                        auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Piscina Infinita",
                                        "Piscina con vista panorámica y servicio de toallas", 0.00, auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Restaurant Gourmet",
                                        "Cocina internacional de alto nivel con chef ejecutivo", 85.00,
                                        auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Bar Premium",
                                        "Coctelería exclusiva y selección de vinos premium", 45.00, auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Servicio de Habitación",
                                        "Cena gourmet en la privacidad de tu habitación", 65.00, auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Salones de Eventos",
                                        "Salones exclusivos para bodas, conferencias y eventos corporativos", 500.00,
                                        auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Transporte Privado",
                                        "Transfer aeropuerto y tours personalizados en vehículos de lujo", 150.00,
                                        auditoriaService);

                        crearServicioSiNoExiste(servicioRepository, "Servicio de Conserjería",
                                        "Asistencia personalizada para reservas, tours y cualquier necesidad.", 0.00,
                                        auditoriaService);

                        System.out.println("✅ Verificación de servicios completada");
                };
        }

        private void crearUsuarioSiNoExiste(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        String username, String password, String rol, AuditoriaService auditoriaService) {
                if (usuarioRepository.findByUsername(username).isEmpty()) {
                        Usuario usuario = new Usuario();
                        usuario.setUsername(username);
                        usuario.setPassword(passwordEncoder.encode(password));
                        usuario.setRol(rol);
                        usuario.setActivo(true);
                        usuarioRepository.save(usuario);
                        System.out.println("👤 Usuario creado: " + username + " (" + rol + ")");
                        auditoriaService.registrarAccion("SYSTEM", "CREACION_USUARIO",
                                        "Usuario creado al inicio: " + username, "Usuario", usuario.getId());
                }
        }

        private void crearHabitacion(com.gestion.hotelera.repository.HabitacionRepository repo, String numero,
                        String tipo,
                        Double precioPorNoche, AuditoriaService auditoriaService) {
                com.gestion.hotelera.model.Habitacion h = new com.gestion.hotelera.model.Habitacion();
                h.setNumero(numero);
                h.setTipo(tipo);
                h.setPrecioPorNoche(precioPorNoche);
                h.setEstado("DISPONIBLE");
                repo.save(h);
                auditoriaService.registrarAccion("SYSTEM", "CREACION_HABITACION",
                                "Habitación inicial creada: " + numero, "Habitacion", h.getId());
        }

        private void crearServicioSiNoExiste(ServicioRepository repo, String nombre, String descripcion,
                        Double precio, AuditoriaService auditoriaService) {

                boolean existe = repo.findAll().stream().anyMatch(s -> s.getNombre().equals(nombre));
                if (!existe) {
                        Servicio s = new Servicio();
                        s.setNombre(nombre);
                        s.setDescripcion(descripcion);
                        s.setPrecio(precio);
                        s.setActivo(true);
                        repo.save(s);
                        System.out.println("📦 Servicio creado: " + nombre);
                        auditoriaService.registrarAccion("SYSTEM", "CREACION_SERVICIO",
                                        "Servicio inicial creado: " + nombre, "Servicio", s.getId());
                }
        }
}