package net.studioxai.studioxBe.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.user.dto.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.user.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.user.entity.VerifiedEmail;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.EmailVerificationTokenRepository;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.user.repository.VerifiedEmailRepository;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    @Value("${server.server-url}")
    private String serverUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    private final JavaMailSender mailSender;

    public void checkEmailVerification(String email) {
        verifiedEmailRepository.findById(email).orElseThrow(
                () -> new UserExceptionHandler(UserErrorCode.EMAIL_NOT_FOUND)
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
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findById(email)
                .orElseThrow(
                        () -> new UserExceptionHandler(UserErrorCode.VERIFICATION_NOT_FOUND)
                );

        emailVerificationToken.validateToken(token);

        VerifiedEmail verifiedEmail = VerifiedEmail.create(emailVerificationToken.getEmail());
        verifiedEmailRepository.save(verifiedEmail);

        // TODO: 여기서 callbackURL get해오는게... 냄새가 폴폴나는데 어떻게 수정하는게 좋을까요?
        return emailVerificationToken.getCallbackUrl();

    }

    private void validateDuplicateSignup(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserExceptionHandler(UserErrorCode.USER_ALREADY_REGISTERS);
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

    // TODO: 여기 메서드가 너무 긴 거 같아서... 고민됩니다
    private void sendEmail(String currentUrl, EmailVerificationRequest emailVerificationRequest, String token) {
        String url = serverUrl + currentUrl + "?email=" + emailVerificationRequest.email() + "?token=" + token;

        String subject = "[STUDIO-X] Email Verification";

        String body = """
        Hello, this is STUDIO-X.
    
        Please click the link below to complete your email verification.
        Verification link: %s
    
        If you did not request this email, you can safely ignore it.
        This link will expire after a certain period of time.
        """.formatted(url);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(emailVerificationRequest.email());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new UserExceptionHandler(UserErrorCode.FAIL_SENDING_MAIL);
        }
    }

}
