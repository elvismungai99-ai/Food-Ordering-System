package com.foodordering;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

    @PostConstruct
    public void init() {
        // Ensure default JVM timezone is aligned with East Africa Time (Africa/Nairobi, UTC+3)
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Nairobi"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
