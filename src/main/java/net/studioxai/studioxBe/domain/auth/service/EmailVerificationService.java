package net.studioxai.studioxBe.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.request.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.EmailValidationResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.TokenResponse;
import net.studioxai.studioxBe.domain.auth.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmail;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.auth.repository.EmailVerificationTokenRepository;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.auth.repository.VerifiedEmailRepository;
import net.studioxai.studioxBe.domain.user.service.UserService;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import net.studioxai.studioxBe.infra.redis.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final UserService userService;
    @Value("${server.server-url}")
    private String serverUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    private final JavaMailSender mailSender;

    public void checkEmailVerification(String email) {
        verifiedEmailRepository.findById(email).orElseThrow(
                () -> new AuthExceptionHandler(AuthErrorCode.EMAIL_NOT_VERIFIED)
        );
    }

    @Transactional
    public void sendEmail(EmailVerificationRequest emailVerificationRequest, String currentUrl) {
        validateDuplicateSignup(emailVerificationRequest.email());
        String token = createEmailToken(emailVerificationRequest);
        sendEmail(currentUrl, emailVerificationRequest, token);
    }

    @Transactional
    public String verifyEmail(String email, String token) {
        EmailVerificationToken tokenEntity = getAndValidateToken(email, token);
        createVerifiedEmail(tokenEntity);
        return tokenEntity.getCallbackUrl();
    }

    public EmailValidationResponse getEmailValidation(String email) {
        boolean isAvailable = verifiedEmailRepository.findById(email).isPresent();
        return EmailValidationResponse.create(email, isAvailable);
    }

    private void validateDuplicateSignup(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthExceptionHandler(AuthErrorCode.USER_ALREADY_REGISTERS);
        }
    }

    private String createEmailToken(EmailVerificationRequest emailVerificationRequest) {
        String token = jwtProvider.createEmailToken(emailVerificationRequest.email());

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.create(
                emailVerificationRequest.email(),
                token,
                emailVerificationRequest.callbackUrl(),
                jwtProperties.mailTokenExpirationMs()
        );

        emailVerificationTokenRepository.save(emailVerificationToken);
        return token;
    }

    private void sendEmail(String currentUrl, EmailVerificationRequest emailVerificationRequest, String token) {
        String verificationUrl = buildVerificationUrl(currentUrl, emailVerificationRequest.email(), token);
        String subject = "[STUDIO-X] 이메일 인증 안내";
        String body = createEmailBody(verificationUrl);
        sendMessage(emailVerificationRequest.email(), subject, body);
    }

    private String buildVerificationUrl(String currentUrl, String email, String token) {
        return UriComponentsBuilder.fromUriString(serverUrl)
                .path(currentUrl)
                .queryParam("email", email)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String createEmailBody(String verificationUrl) {
        return """
        안녕하세요, STUDIO-X입니다.
        
        아래 링크를 클릭하여 이메일 인증을 완료해 주세요.
        인증 링크: %s
        
        본인이 요청한 인증이 아니라면, 이 메일은 무시하셔도 됩니다.
        해당 링크는 일정 시간이 지나면 자동으로 만료됩니다.
        """.formatted(verificationUrl);
    }

    private void sendMessage(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new AuthExceptionHandler(AuthErrorCode.FAIL_SENDING_MAIL);
        }
    }

    private EmailVerificationToken getAndValidateToken(String email, String token) {
        EmailVerificationToken emailVerificationToken =
                emailVerificationTokenRepository.findById(email)
                        .orElseThrow(() -> new AuthExceptionHandler(AuthErrorCode.VERIFICATION_NOT_FOUND));

        emailVerificationToken.validateToken(token);
        return emailVerificationToken;
    }

    private void createVerifiedEmail(EmailVerificationToken tokenEntity) {
        VerifiedEmail verifiedEmail = VerifiedEmail.create(tokenEntity.getEmail());
        verifiedEmailRepository.save(verifiedEmail);
    }
}
