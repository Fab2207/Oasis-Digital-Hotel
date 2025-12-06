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
            crearDescuentoSiNoExiste(descuentoRepository, "BIENVENIDA", "FIJO", 50.0, 50);
            System.out.println("✅ Descuentos verificados");
        };
    }

    private void crearDescuentoSiNoExiste(DescuentoRepository repo, String codigo, String tipo, Double valor,
            Integer usosMax) {
        if (repo.findByCodigo(codigo).isEmpty()) {
            Descuento d = new Descuento();
            d.setCodigo(codigo);
            d.setTipo(tipo);
            d.setValor(valor);
            d.setUsosMaximos(usosMax);
            d.setUsosActuales(0);
            d.setActivo(true);
            d.setDescripcion("Descuento promocional " + codigo);
            d.setFechaInicio(LocalDate.now());
            // Fecha fin en 1 año
            d.setFechaFin(LocalDate.now().plusYears(1));
            repo.save(d);
            System.out.println("🏷️ Descuento creado: " + codigo);
        }
    }
}
