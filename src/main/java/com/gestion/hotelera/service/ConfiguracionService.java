package com.gestion.hotelera.service;

import com.gestion.hotelera.model.ConfiguracionGlobal;
import com.gestion.hotelera.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;
    private static final String CLAVE_DEFAULT = "HOTEL_DEFAULT";

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracionGlobal obtenerConfiguracion() {
        return configuracionRepository.findByClave(CLAVE_DEFAULT)
                .orElseGet(() -> {
                    
                    ConfiguracionGlobal config = new ConfiguracionGlobal();
                    config.setClave(CLAVE_DEFAULT);
                    config.setNombreHotel("Oasis Digital Resort");
                    config.setColorPrimario("#00A67E"); 
                    config.setColorAcento("#D4AF37"); 
                    config.setBorderRadius("0.5rem");
                    config.setMensajeBienvenida("Bienvenido al sistema");
                    return config;
                });
    }

    @Transactional
    public ConfiguracionGlobal guardarConfiguracion(ConfiguracionGlobal nuevaConfig) {
        ConfiguracionGlobal actual = obtenerConfiguracion();

        if (nuevaConfig.getNombreHotel() != null)
            actual.setNombreHotel(nuevaConfig.getNombreHotel());
        if (nuevaConfig.getColorPrimario() != null)
            actual.setColorPrimario(nuevaConfig.getColorPrimario());
        if (nuevaConfig.getColorAcento() != null)
            actual.setColorAcento(nuevaConfig.getColorAcento());
        if (nuevaConfig.getBorderRadius() != null)
            actual.setBorderRadius(nuevaConfig.getBorderRadius());

        if (nuevaConfig.getEmailContacto() != null)
            actual.setEmailContacto(nuevaConfig.getEmailContacto());
        if (nuevaConfig.getDireccion() != null)
            actual.setDireccion(nuevaConfig.getDireccion());
        if (nuevaConfig.getMensajeBienvenida() != null)
            actual.setMensajeBienvenida(nuevaConfig.getMensajeBienvenida());
        if (nuevaConfig.getUrlLogo() != null)
            actual.setUrlLogo(nuevaConfig.getUrlLogo());

        actual.setEmailReserva(nuevaConfig.isEmailReserva());
        actual.setAlertasSeguridad(nuevaConfig.isAlertasSeguridad());

        if (actual.getId() == null) {
            actual.setClave(CLAVE_DEFAULT);
        }

        return configuracionRepository.save(actual);
    }
}
