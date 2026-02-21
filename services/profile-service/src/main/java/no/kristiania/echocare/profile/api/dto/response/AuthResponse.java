package no.kristiania.echocare.profile.api.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuthResponse(
       @NotNull String token,
       @NotNull UUID caregiverId,
       @NotBlank String username
) {
}
