package com.foodordering.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.foodordering.User.entity.Role;
import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        String requestedRole = request.getRole();
        String finalRole;
        if (Role.CUSTOMER.equals(requestedRole) || Role.OWNER.equals(requestedRole)) {
            finalRole = requestedRole;
        } else {
            finalRole = Role.CUSTOMER; // safe default — blocks SUPER_ADMIN or any invalid value
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setRole(finalRole);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return new AuthResponse(token, savedUser.getId(), savedUser.getRole(), savedUser.getFirstName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getRole(), user.getFirstName());
    }
}