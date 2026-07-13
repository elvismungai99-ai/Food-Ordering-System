package com.foodordering.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        String rawPassword = "Elvis123";
        String hash = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println(hash);
    }
}
