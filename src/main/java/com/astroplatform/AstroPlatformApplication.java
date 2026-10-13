package com.astroplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.astroplatform.repository")
@EntityScan(basePackages = "com.astroplatform.entity")
public class AstroPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstroPlatformApplication.class, args);
    }

}
