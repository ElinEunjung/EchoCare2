package no.kristiania.echocare.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.profile.api.dto.requests.LoginRequest;
import no.kristiania.echocare.profile.api.dto.requests.RegisterRequest;
import no.kristiania.echocare.profile.api.dto.response.AuthResponse;
import no.kristiania.echocare.profile.config.JwtUtil;
import no.kristiania.echocare.profile.domain.entity.Caregiver;
import no.kristiania.echocare.profile.repository.CaregiverRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final CaregiverRepository caregiverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check if username already exists
        if (caregiverRepository.existsByUsername(request.username())) {
            log.warn("Registration failed: Username already exists: {}", request.username());
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (caregiverRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new caregiver
        Caregiver caregiver = new Caregiver();
        caregiver.setUsername(request.username());
        caregiver.setPasswordHash(passwordEncoder.encode(request.password()));
        caregiver.setEmail(request.email());

        caregiver = caregiverRepository.save(caregiver);
        log.info("User registered successfully: {} (ID: {})", caregiver.getUsername(), caregiver.getId());

        // Generate JWT token
        String token = jwtUtil.generateToken(caregiver.getId(), caregiver.getUsername());

        return new AuthResponse(token, caregiver.getId(), caregiver.getUsername());
    }


    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.username());

        // Find caregiver by username
        Caregiver caregiver = caregiverRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Verify password
        if (!passwordEncoder.matches(request.password(), caregiver.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        log.info("User logged in successfully: {}", caregiver.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(caregiver.getId(), caregiver.getUsername());

        return new AuthResponse(token, caregiver.getId(), caregiver.getUsername());
    }
}
