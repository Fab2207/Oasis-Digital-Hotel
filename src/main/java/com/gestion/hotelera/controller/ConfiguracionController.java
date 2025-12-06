package com.gestion.hotelera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final com.gestion.hotelera.service.NotificacionService notificacionService;
    private final com.gestion.hotelera.repository.UsuarioRepository usuarioRepository;

    public ConfiguracionController(com.gestion.hotelera.service.NotificacionService notificacionService,
            com.gestion.hotelera.repository.UsuarioRepository usuarioRepository) {
        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarConfiguracion(org.springframework.ui.Model model) {
        model.addAttribute("configuracion", new ConfiguracionForm());
        model.addAttribute("notificaciones", notificacionService.obtenerTodas());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "configuracion";
    }

    @org.springframework.web.bind.annotation.PostMapping("/usuario/guardar")
    public String guardarUsuario(@org.springframework.web.bind.annotation.RequestParam long id,
            @org.springframework.web.bind.annotation.RequestParam String rol) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setRol(rol);
            usuarioRepository.save(usuario);
        });
        return "redirect:/configuracion#section-seguridad";
    }

    @org.springframework.web.bind.annotation.PostMapping("/usuario/toggle-estado")
    public String toggleEstadoUsuario(@org.springframework.web.bind.annotation.RequestParam String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            if (!"admin".equals(usuario.getUsername())) { // Prevent admin lock
                usuario.setActivo(!usuario.getActivo());
                usuarioRepository.save(usuario);
            }
        });
        return "redirect:/configuracion#section-seguridad";
    }

    @org.springframework.web.bind.annotation.PostMapping("/guardar")
    public String guardarConfiguracion(
            @org.springframework.web.bind.annotation.ModelAttribute ConfiguracionForm configuracion) {
        // Here you would typically save the configuration to a database or service
        // For now, we just redirect back to the configuration page
        return "redirect:/configuracion";
    }

    public static class ConfiguracionForm {
        private String nombreHotel = "Oasis Digital Resort";
        private String emailContacto = "contacto@oasisdigital.com";
        private String direccion = "Av. del Sol 123, Cancún, México";
        private String idioma = "es";
        private String zonaHoraria = "America/Mexico_City";

        // Getters and Setters
        public String getNombreHotel() {
            return nombreHotel;
        }

        public void setNombreHotel(String nombreHotel) {
            this.nombreHotel = nombreHotel;
        }

        public String getEmailContacto() {
            return emailContacto;
        }

        public void setEmailContacto(String emailContacto) {
            this.emailContacto = emailContacto;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getIdioma() {
            return idioma;
        }

        public void setIdioma(String idioma) {
            this.idioma = idioma;
        }

        public String getZonaHoraria() {
            return zonaHoraria;
        }

        public void setZonaHoraria(String zonaHoraria) {
            this.zonaHoraria = zonaHoraria;
        }
    }
}
