package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Descuento;
import com.gestion.hotelera.service.DescuentoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/descuentos")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DescuentoApiController {

    private final DescuentoService descuentoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'CLIENTE')")
    public List<Descuento> listar() {
        System.out.println("DescuentoApiController.listar() - Solicitud recibida");
        List<Descuento> lista = descuentoService.obtenerTodosLosDescuentos();
        System.out.println("DescuentoApiController.listar() - Encontrados: " + lista.size());
        return lista;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Descuento descuento = new Descuento();
            descuento.setCodigo((String) payload.get("codigo"));
            descuento.setDescripcion((String) payload.get("descripcion"));
            descuento.setTipo((String) payload.get("tipo"));

            if (payload.get("valor") != null)
                descuento.setValor(Double.valueOf(payload.get("valor").toString()));
            if (payload.get("montoMinimo") != null)
                descuento.setMontoMinimo(Double.valueOf(payload.get("montoMinimo").toString()));
            if (payload.get("montoMaximoDescuento") != null)
                descuento.setMontoMaximoDescuento(Double.valueOf(payload.get("montoMaximoDescuento").toString())); 

            if (payload.get("fechaInicio") != null)
                descuento.setFechaInicio(java.time.LocalDate.parse(payload.get("fechaInicio").toString()));
            if (payload.get("fechaFin") != null)
                descuento.setFechaFin(java.time.LocalDate.parse(payload.get("fechaFin").toString()));

            if (payload.get("usosMaximos") != null)
                descuento.setUsosMaximos(Integer.valueOf(payload.get("usosMaximos").toString()));

            descuento.setActivo(Boolean.TRUE);
            if (payload.containsKey("activo")) {
                descuento.setActivo(Boolean.valueOf(payload.get("activo").toString()));
            }

            return ResponseEntity.ok(descuentoService.crearDescuento(descuento));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error procesando datos: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Descuento> actualizar(@PathVariable Long id, @RequestBody Descuento descuento) {
        descuento.setId(id);
        return ResponseEntity.ok(descuentoService.actualizarDescuento(descuento));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        descuentoService.eliminarDescuento(id);
        return ResponseEntity.noContent().build();
    }
}
