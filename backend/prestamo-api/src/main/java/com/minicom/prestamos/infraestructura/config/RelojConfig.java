package com.minicom.prestamos.infraestructura.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }
}
