package com.gestion.hotelera;

import com.gestion.hotelera.config.JwtProperties;
import com.gestion.hotelera.config.MailConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, MailConfigurationProperties.class })
@EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class HoteleraApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoteleraApplication.class, args);
    }
}
