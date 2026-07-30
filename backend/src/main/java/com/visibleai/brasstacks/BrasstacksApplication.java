package com.visibleai.brasstacks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class BrasstacksApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application
                .properties(
                    "spring.profiles.active=prod",
                    "spring.config.additional-location=optional:file:/home/ifaru02/brasstacks/"
                )
                .sources(BrasstacksApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BrasstacksApplication.class, args);
    }
}
