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
import org.springframework.web.util.UriComponentsBuilder;

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
                () -> new UserExceptionHandler(UserErrorCode.EMAIL_NOT_VERIFIED)
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
            throw new UserExceptionHandler(UserErrorCode.FAIL_SENDING_MAIL);
        }
    }
}
