package com.logunify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LogunifyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogunifyServiceApplication.class, args);
    }

}
