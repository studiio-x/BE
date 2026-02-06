package net.studioxai.studioxBe.domain.auth.dto.response;

public record EmailValidationResponse(
        String email,
        boolean isAvailable
) {
    public static EmailValidationResponse create(String email, boolean isAvailable) {
        return new EmailValidationResponse(email, isAvailable);
    }
}
