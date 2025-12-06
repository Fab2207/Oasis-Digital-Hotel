package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.EmpleadoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empleados")
@SuppressWarnings("null")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoController(EmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoRepository.findAll());
        return "listaEmpleados";
    }

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        Empleado empleado = new Empleado();
        empleado.setUsuario(new Usuario()); // Initialize Usuario to avoid NPE in form
        model.addAttribute("empleado", empleado);
        return "registrarEmpleado";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(@ModelAttribute Empleado empleado,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Handle Usuario creation
        Usuario usuario = empleado.getUsuario();
        if (usuario == null) {
            usuario = new Usuario();
            empleado.setUsuario(usuario);
        }

        // Set username to email if not provided
        if (usuario.getUsername() == null || usuario.getUsername().isEmpty()) {
            usuario.setUsername(empleado.getEmail());
        }

        // Encode password
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        // Set role
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_RECEPCIONISTA");
        } else if (!usuario.getRol().startsWith("ROLE_")) {
            usuario.setRol("ROLE_" + usuario.getRol());
        }

        usuario.setActivo(true);

        empleadoRepository.save(empleado);
        redirectAttributes.addFlashAttribute("successMessage", "Empleado registrado exitosamente.");
        return "redirect:/empleados";
    }

    @GetMapping("/editar/{id}")
    public String editarEmpleado(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("empleado", empleado);
        return "registrarEmpleado";
    }

    @GetMapping("/ver/{id}")
    public String verEmpleado(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("empleado", empleado);
        return "empleado-ver";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado(@PathVariable Long id, @ModelAttribute Empleado empleadoDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));

        empleado.setNombres(empleadoDetails.getNombres());
        empleado.setApellidos(empleadoDetails.getApellidos());
        empleado.setEmail(empleadoDetails.getEmail());
        empleado.setTelefono(empleadoDetails.getTelefono());

        // Update Usuario role if needed
        if (empleado.getUsuario() != null && empleadoDetails.getUsuario() != null) {
            String newRol = empleadoDetails.getUsuario().getRol();
            if (newRol != null && !newRol.isEmpty()) {
                if (!newRol.startsWith("ROLE_")) {
                    newRol = "ROLE_" + newRol;
                }
                empleado.getUsuario().setRol(newRol);
            }
            // Password update logic only if provided (though readonly in view, good to have
            // safeguard)
            if (empleadoDetails.getUsuario().getPassword() != null
                    && !empleadoDetails.getUsuario().getPassword().isEmpty()) {
                empleado.getUsuario().setPassword(passwordEncoder.encode(empleadoDetails.getUsuario().getPassword()));
            }
        }

        empleadoRepository.save(empleado);
        redirectAttributes.addFlashAttribute("successMessage", "Empleado actualizado exitosamente.");
        return "redirect:/empleados";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        empleadoRepository.delete(empleado);
        redirectAttributes.addFlashAttribute("successMessage", "Empleado eliminado exitosamente.");
        return "redirect:/empleados";
    }
}
