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

        public SecurityConfig(
                        AuthenticationProvider authenticationProvider,
                        RateLimitingFilter rateLimitingFilter,
                        com.gestion.hotelera.security.JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.authenticationProvider = authenticationProvider;
                this.rateLimitingFilter = rateLimitingFilter;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

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
                                                
                                                .requestMatchers("/api/auth/**", "/api/resenas/aprobadas/**")
                                                .permitAll()

                                                .requestMatchers("/api/habitaciones/**")
                                                .permitAll()
                                                
                                                .requestMatchers("/api/configuracion/public")
                                                .permitAll()
                                                
                                                .requestMatchers("/api/configuracion/**")
                                                .hasRole("ADMIN")
                                                
                                                .requestMatchers("/api/reservas/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")
                                                
                                                .requestMatchers("/api/servicios/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")
                                                
                                                .requestMatchers("/api/descuentos/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_CLIENTE")
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

                                                .requestMatchers("/**").permitAll()

                                )
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .contentTypeOptions(content -> {
                                                })
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true)))
                                .build();
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
