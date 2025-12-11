package com.gestion.hotelera.config;

import com.gestion.hotelera.model.Descuento;
import com.gestion.hotelera.repository.DescuentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DiscountInitializer {

    @Bean
    public CommandLineRunner initDiscounts(DescuentoRepository descuentoRepository) {
        return args -> {
            System.out.println("🏷️ Verificando descuentos (DiscountInitializer)...");
            crearDescuentoSiNoExiste(descuentoRepository, "VERANO2025", "PORCENTAJE", 20.0, 100);
            crearDescuentoSiNoExiste(descuentoRepository, "BIENVENIDA", "MONTO_FIJO", 50.0, 50);
            System.out.println("✅ Descuentos verificados");
        };
    }

    private void crearDescuentoSiNoExiste(DescuentoRepository repo, String codigo, String tipo, Double valor,
            Integer usosMax) {
        Descuento d = repo.findByCodigo(codigo).orElse(new Descuento());

        if (d.getId() == null || !d.getTipo().equals(tipo) || !d.getValor().equals(valor)) {
            d.setCodigo(codigo);
            d.setTipo(tipo);
            d.setValor(valor);
            d.setUsosMaximos(usosMax);
            if (d.getUsosActuales() == null)
                d.setUsosActuales(0);
            if (d.getActivo() == null)
                d.setActivo(true);

            if (d.getDescripcion() == null)
                d.setDescripcion("Descuento promocional " + codigo);

            if (d.getFechaInicio() == null)
                d.setFechaInicio(LocalDate.now());
            if (d.getFechaFin() == null)
                d.setFechaFin(LocalDate.now().plusYears(1));

            repo.save(d);
            System.out.println("🏷️ Descuento actualizado/creado: " + codigo + " (" + tipo + ")");
        }
    }
}
