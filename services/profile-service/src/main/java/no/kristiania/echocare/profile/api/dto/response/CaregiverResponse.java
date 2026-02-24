package no.kristiania.echocare.profile.api.dto.response;

import java.util.UUID;

public record CaregiverResponse(
        UUID id,
        String username,
        String email
) {}

