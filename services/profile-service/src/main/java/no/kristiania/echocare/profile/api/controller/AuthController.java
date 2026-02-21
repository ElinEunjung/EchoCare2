package no.kristiania.echocare.profile.api.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.LoginRequest;
import no.kristiania.echocare.profile.api.dto.requests.RegisterRequest;
import no.kristiania.echocare.profile.api.dto.response.AuthResponse;
import no.kristiania.echocare.profile.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
