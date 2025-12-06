package com.gestion.hotelera;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base para tests de integración
 * Proporciona configuración común para todos los tests de integración
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    // Esta clase puede contener métodos de utilidad comunes para tests
}