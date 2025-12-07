package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthLoginController {

    private static final Logger logger = LoggerFactory.getLogger(AuthLoginController.class);

    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final HabitacionService habitacionService;
    private final com.gestion.hotelera.service.ServicioService servicioService;

    public AuthLoginController(ClienteService clienteService, ReservaService reservaService,
            HabitacionService habitacionService, com.gestion.hotelera.service.ServicioService servicioService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.habitacionService = habitacionService;
        this.servicioService = servicioService;
    }

    @GetMapping({ "/", "/index" })
    public String mostrarIndex(Model model, Authentication auth) {
        // Redirigir al dashboard si es personal administrativo
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_RECEPCIONISTA"));
            if (isStaff) {
                return "redirect:/dashboard";
            }
        }

        boolean isLoggedIn = false;
        String username = "";
        String rol = "";

        try {
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                isLoggedIn = true;
                username = auth.getName();

                // Intentar obtener información del cliente
                try {
                    Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
                    if (cliente != null) {
                        username = cliente.getNombres();
                        rol = "ROLE_CLIENTE";

                        // Agregar conteos de reservas para el cliente
                        long totalReservas = reservaService.contarReservasPorCliente(auth.getName());
                        long reservasActivas = reservaService.contarReservasActivasPorCliente(auth.getName());
                        long reservasFinalizadas = reservaService.contarReservasFinalizadasPorCliente(auth.getName());

                        model.addAttribute("totalReservas", totalReservas);
                        model.addAttribute("reservasActivas", reservasActivas);
                        model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                    }
                } catch (Exception e) {
                    // Si no es cliente, mantener el username original
                }
            }
        } catch (Exception e) {
            isLoggedIn = false;
            username = "";
            rol = "";
        }

        // Cargar habitaciones disponibles para mostrar en el index
        try {
            List<Habitacion> todasHabitaciones = habitacionService.obtenerTodasLasHabitaciones();

            // Agrupar por tipo y obtener una muestra de cada tipo
            Map<String, List<Habitacion>> habitacionesPorTipo = todasHabitaciones.stream()
                    .filter(h -> "DISPONIBLE".equalsIgnoreCase(h.getEstado()))
                    .collect(Collectors.groupingBy(Habitacion::getTipo));

            // Obtener una habitación de cada tipo para mostrar en el index
            model.addAttribute("habitacionSimple",
                    habitacionesPorTipo.getOrDefault("Simple", java.util.Collections.emptyList()).stream().findFirst()
                            .orElse(null));
            model.addAttribute("habitacionDoble",
                    habitacionesPorTipo.getOrDefault("Doble", java.util.Collections.emptyList()).stream().findFirst()
                            .orElse(null));
            model.addAttribute("suiteJunior",
                    habitacionesPorTipo.getOrDefault("Suite", java.util.Collections.emptyList()).stream().findFirst()
                            .orElse(null));
            model.addAttribute("suitePresidencial",
                    habitacionesPorTipo.getOrDefault("Deluxe", java.util.Collections.emptyList()).stream().findFirst()
                            .orElse(null));

        } catch (Exception e) {
            // Log del error pero continuar mostrando la página
            logger.error("Error cargando habitaciones: " + e.getMessage());
        }

        // Cargar Servicios
        try {
            model.addAttribute("servicios", servicioService.listarTodos());
        } catch (Exception e) {
            logger.error("Error cargando servicios: " + e.getMessage());
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("username", username);
        model.addAttribute("rol", rol);
        return "index";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/registro")
    public String showRegisterPage(Model model) {
        return "redirect:/login?register=true";
    }

    @PostMapping("/registro")
    public String registrarCliente(
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("dni") String dni,
            @RequestParam("email") String email,
            @RequestParam("telefono") String telefono,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        try {
            // Validaciones básicas
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de usuario es requerido");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es requerida");
            }

            // Crear el usuario (se guardará dentro del servicio de cliente)
            Usuario usuario = new Usuario();
            usuario.setUsername(username.trim());
            usuario.setPassword(password); // Se encriptará en el servicio
            // El rol y activo se setean en el servicio

            // Crear el cliente
            Cliente cliente = new Cliente();
            cliente.setNombres(nombres.trim());
            cliente.setApellidos(apellidos.trim());
            cliente.setDni(dni.trim());
            cliente.setEmail(email != null ? email.trim() : null);
            cliente.setTelefono(telefono != null ? telefono.trim() : null);
            cliente.setUsuario(usuario);

            // Delegar la creación al servicio
            clienteService.crearCliente(cliente);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Registro exitoso. Ahora puedes iniciar sesión con tu usuario: " + username);
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login?register=true&registerError=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al registrar: " + e.getMessage());
            return "redirect:/login?register=true&registerError=true";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    @PostMapping("/contacto/enviar")
    public String enviarMensajeContacto(
            @RequestParam("name") String nombre,
            @RequestParam("email") String email,
            @RequestParam("phone") String telefono,
            @RequestParam("message") String mensaje,
            RedirectAttributes redirectAttributes) {

        logger.info("Nuevo mensaje de contacto recibido - Nombre: {}, Email: {}, Teléfono: {}",
                nombre, email, telefono);

        redirectAttributes.addFlashAttribute("successMessage",
                "¡Gracias " + nombre + "! Hemos recibido tu mensaje y te contactaremos pronto.");

        return "redirect:/#contacto";
    }
}
