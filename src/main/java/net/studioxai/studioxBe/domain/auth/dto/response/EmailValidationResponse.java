package net.studioxai.studioxBe.domain.auth.dto.response;

public record EmailValidationResponse(
        String email,
        boolean isVerified
) {
    public static EmailValidationResponse create(String email, boolean isVerified) {
        return new EmailValidationResponse(email, isVerified);
    }
}
