package cocha.vive.backend.auth;

public record AuthResponse(String internalToken, boolean requireOnboarding) {
}
