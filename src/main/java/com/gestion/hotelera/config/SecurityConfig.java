package com.gestion.hotelera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

        private final AuthenticationProvider authenticationProvider;
        private final RateLimitingFilter rateLimitingFilter;
        private final com.gestion.hotelera.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @org.springframework.core.annotation.Order(1)
        @Bean
        public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .securityMatcher("/api/**")
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                // API pública
                                                .requestMatchers("/api/auth/**", "/api/resenas/aprobadas/**")
                                                .permitAll()
                                                // Habitaciones: público para que el frontend pueda consultar
                                                // disponibilidad
                                                .requestMatchers("/api/habitaciones/**")
                                                .permitAll()
                                                // Reservas: ADMIN y RECEPCIONISTA todas, CLIENTE solo las suyas
                                                .requestMatchers("/api/reservas/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")
                                                // Servicios: todos autenticados
                                                .requestMatchers("/api/servicios/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")
                                                // Descuentos: solo ADMIN
                                                .requestMatchers("/api/descuentos/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .build();
        }

        @org.springframework.core.annotation.Order(2)
        @Bean
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                // ==== RUTAS PÚBLICAS ====
                                                .requestMatchers("/", "/index", "/home", "/login", "/registro",
                                                                "/logout",
                                                                "/css/**", "/js/**", "/images/**",
                                                                "/h2-console/**",
                                                                "/habitaciones/publico", "/resenas/aprobadas",
                                                                "/actuator/health", "/actuator/info")
                                                .permitAll()

                                                // API endpoints para obtener habitaciones disponibles (debe estar antes
                                                // de reglas específicas)
                                                .requestMatchers("/api/**")
                                                .permitAll()

                                                // ==== ADMIN - CONTROL TOTAL ====
                                                // Gestión de catálogo: Crear, editar y eliminar Habitaciones y
                                                // Servicios
                                                .requestMatchers("/habitaciones/guardar", "/habitaciones/eliminar/**",
                                                                "/habitaciones/editar/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers("/servicios/nuevo", "/servicios/editar/**",
                                                                "/servicios/guardar", "/servicios/*/eliminar")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Control financiero: Administra Descuentos/Cupones y ve todos los
                                                // ingresos
                                                // Control financiero: Administra Descuentos/Cupones y ve todos los
                                                // ingresos
                                                .requestMatchers("/descuentos/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // Dashboard: Accede a estadísticas en tiempo real (ocupación, ingresos,
                                                // ventas)
                                                .requestMatchers("/reportes/**", "/actuator/**", "/monitoreo/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Seguridad y Auditoría: Visualiza los Logs
                                                .requestMatchers("/auditoria/**", "/auditoria/logs/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Moderación: Aprueba o rechaza las Reseñas
                                                .requestMatchers("/resenas/pendientes", "/resenas/aprobar/**",
                                                                "/resenas/rechazar/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Gestión de empleados
                                                .requestMatchers("/empleados/**", "/admin/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // ==== RECEPCIONISTA - OPERACIÓN DIARIA ====
                                                // ADMIN puede realizar todas las funciones del Recepcionista
                                                // Gestión de Reservas: Crea, modifica y cancela reservas para cualquier
                                                // cliente

                                                // Excepción: Permitir a clientes acceder a servicios de sus propias
                                                // reservas y ver sus facturas
                                                .requestMatchers("/reservas/*/servicios", "/reservas/*/servicios/**",
                                                                "/reservas/factura/*", "/reservas/*/pago")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")

                                                .requestMatchers("/reservas/**", "/recepcion/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // Ciclo de Huésped: Ejecuta el Check-in (entrada) y el Check-out
                                                // (salida)
                                                .requestMatchers("/reservas/checkin/**", "/reservas/checkout/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // Cancelación: Permitir a clientes cancelar sus propias reservas
                                                .requestMatchers("/reservas/cancelar/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")

                                                .requestMatchers("/reservas/**", "/recepcion/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // Atención y Clientes: Registra nuevos clientes y consulta el historial
                                                // de huéspedes
                                                .requestMatchers("/clientes/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // Disponibilidad: Verifica la disponibilidad de habitaciones en tiempo
                                                // real
                                                .requestMatchers("/habitaciones", "/servicios", "/calendario/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                                                // ==== CLIENTE - AUTOGESTIÓN ====
                                                // Autogestión: Se registra, inicia sesión (Login) y gestiona los datos
                                                // de su cuenta
                                                .requestMatchers("/cliente/**")
                                                .hasAuthority("ROLE_CLIENTE")

                                                // Reservas Propias: Busca disponibilidad y realiza sus propias reservas
                                                .requestMatchers("/cliente/reservas/**")
                                                .hasAuthority("ROLE_CLIENTE")

                                                // Historial: Consulta todas sus reservas pasadas y futuras
                                                .requestMatchers("/cliente/historial")
                                                .hasAuthority("ROLE_CLIENTE")

                                                // Feedback: Deja reseñas y calificaciones sobre su estancia (sujetas a
                                                // moderación)
                                                .requestMatchers("/resenas/crear/**", "/resenas/guardar")
                                                .hasAuthority("ROLE_CLIENTE")

                                                // ==== DASHBOARD - TODOS AUTENTICADOS ====
                                                .requestMatchers("/dashboard")
                                                .authenticated()

                                                // Cualquier otra ruta requiere autenticación
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authenticationProvider(authenticationProvider)
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(roleBasedSuccessHandler())
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .contentTypeOptions(content -> {
                                                })
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true)))
                                .build();
        }

        public SecurityConfig(
                        AuthenticationProvider authenticationProvider,
                        RateLimitingFilter rateLimitingFilter,
                        com.gestion.hotelera.security.JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.authenticationProvider = authenticationProvider;
                this.rateLimitingFilter = rateLimitingFilter;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        private CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(java.util.Arrays.asList(
                                "http://localhost:4200",
                                "http://127.0.0.1:4200",
                                "https://*.vercel.app",
                                "https://*.netlify.app"));
                configuration.setAllowedMethods(java.util.Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(java.util.Arrays.asList(
                                "Authorization", "Content-Type", "X-Requested-With",
                                "Accept", "Origin", "Access-Control-Request-Method",
                                "Access-Control-Request-Headers"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Redirige a los usuarios según su rol después del login:
         * - ADMIN y RECEPCIONISTA -> /dashboard
         * - CLIENTE -> /cliente/area
         */
        @Bean
        public AuthenticationSuccessHandler roleBasedSuccessHandler() {
                return (request, response, authentication) -> {
                        java.util.Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                        boolean isAdmin = authorities.stream()
                                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                        boolean isReceptionist = authorities.stream()
                                        .anyMatch(a -> "ROLE_RECEPCIONISTA".equals(a.getAuthority()));
                        boolean isClient = authorities.stream()
                                        .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

                        if (isAdmin) {
                                response.sendRedirect("/dashboard?loginSuccess=true");
                        } else if (isReceptionist) {
                                response.sendRedirect("/recepcion?loginSuccess=true");
                        } else if (isClient) {
                                response.sendRedirect("/?loginSuccess=true");
                        } else {
                                response.sendRedirect("/?loginSuccess=true");
                        }
                };
        }
}
