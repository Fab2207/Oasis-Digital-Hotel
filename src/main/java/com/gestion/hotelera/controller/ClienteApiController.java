package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.ClienteDTO;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ClienteApiController {
    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAll(@RequestParam(required = false) String search) {
        List<Cliente> clientes;
        if (search != null && !search.trim().isEmpty()) {
            clientes = clienteService.buscarClientes(search);
        } else {
            clientes = clienteService.obtenerTodosLosClientes();
        }

        List<ClienteDTO> dtos = clientes.stream().map(c -> ClienteDTO.builder()
                .id(c.getId())
                .nombres(c.getNombres())
                .apellidos(c.getApellidos())
                .dni(c.getDni())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .nacionalidad(c.getNacionalidad())
                .totalReservas(0L)
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA') or isAuthenticated()")
    public ResponseEntity<ClienteDTO> getById(@PathVariable Long id) {
        return clienteService.obtenerClientePorId(id)
                .map(c -> ClienteDTO.builder()
                        .id(c.getId())
                        .nombres(c.getNombres())
                        .apellidos(c.getApellidos())
                        .dni(c.getDni())
                        .email(c.getEmail())
                        .telefono(c.getTelefono())
                        .nacionalidad(c.getNacionalidad())
                        .totalReservas(0L)
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ClienteDTO> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevo = clienteService.crearCliente(cliente);
        return ResponseEntity.ok(ClienteDTO.builder()
                .id(nuevo.getId())
                .nombres(nuevo.getNombres())
                .apellidos(nuevo.getApellidos())
                .dni(nuevo.getDni())
                .email(nuevo.getEmail())
                .telefono(nuevo.getTelefono())
                .nacionalidad(nuevo.getNacionalidad())
                .totalReservas(0L)
                .build());
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClienteDTO> actualizarCliente(@PathVariable Long id, @RequestBody ClienteDTO dto) {
        return clienteService.obtenerClientePorId(id)
                .map(c -> {
                    
                    c.setNombres(dto.getNombres());
                    c.setApellidos(dto.getApellidos());
                    c.setDni(dto.getDni()); 
                    c.setEmail(dto.getEmail());
                    c.setTelefono(dto.getTelefono());
                    c.setNacionalidad(dto.getNacionalidad());

                    Cliente actualizado = clienteService.actualizarCliente(c);

                    return ClienteDTO.builder()
                            .id(actualizado.getId())
                            .nombres(actualizado.getNombres())
                            .apellidos(actualizado.getApellidos())
                            .dni(actualizado.getDni())
                            .email(actualizado.getEmail())
                            .telefono(actualizado.getTelefono())
                            .nacionalidad(actualizado.getNacionalidad())
                            .totalReservas(0L)
                            .build();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCliente(@PathVariable long id) {
        if (clienteService.eliminarClientePorId(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/profile")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CLIENTE') or isAuthenticated()")
    public ResponseEntity<ClienteDTO> getProfile(org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        Cliente cliente = clienteService.obtenerPorUsername(username);

        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ClienteDTO.builder()
                .id(cliente.getId())
                .nombres(cliente.getNombres())
                .apellidos(cliente.getApellidos())
                .dni(cliente.getDni())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .nacionalidad(cliente.getNacionalidad())
                .totalReservas(0L) 
                .build());
    }
}
