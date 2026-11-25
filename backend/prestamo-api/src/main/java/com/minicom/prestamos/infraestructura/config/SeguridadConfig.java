package com.minicom.prestamos.infraestructura.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.minicom.prestamos.infraestructura.seguridad.DetallesUsuarioServicio;
import com.minicom.prestamos.infraestructura.seguridad.JwtFiltroAutenticacion;
import com.minicom.prestamos.infraestructura.seguridad.JwtServicio;
import com.minicom.prestamos.infraestructura.seguridad.PuntoEntradaAutenticacion;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SeguridadConfig {

    private final JwtServicio jwtServicio;
    private final DetallesUsuarioServicio detallesUsuarioServicio;
    private final PasswordEncoder codificadorContrasena;
    private final PuntoEntradaAutenticacion puntoEntradaAutenticacion;

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider proveedorAutenticacion() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(detallesUsuarioServicio);
        proveedor.setPasswordEncoder(codificadorContrasena);
        return proveedor;
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        JwtFiltroAutenticacion filtroJwt = new JwtFiltroAutenticacion(jwtServicio, detallesUsuarioServicio);

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(puntoEntradaAutenticacion))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean(name = "corsConfigurationSource")
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
