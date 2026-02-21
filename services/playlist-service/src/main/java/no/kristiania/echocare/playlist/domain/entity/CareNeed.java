package no.kristiania.echocare.playlist.domain.entity;

/**
 * Care needs that can be addressed through music therapy
 * Duplicated from profile-service to maintain microservices independence
 */
public enum CareNeed {
    STRESS_RELIEF,
    ACTIVITY_SUPPORT,
    CALMING_AGITATION,
    EASE_DEPRESSION,
    EASE_ANXIETY
}

