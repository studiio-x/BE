package net.studioxai.studioxBe.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.request.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.PasswordCodeVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.PasswordResetCodeRequest;
import net.studioxai.studioxBe.domain.auth.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.auth.entity.PasswordResetCode;
import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmail;
import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmailCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.auth.repository.EmailVerificationTokenRepository;
import net.studioxai.studioxBe.domain.auth.repository.PasswordResetCodeRepository;
import net.studioxai.studioxBe.domain.auth.repository.VerifiedEmailCodeRepository;
import net.studioxai.studioxBe.domain.auth.util.ResetCodeGenerator;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.auth.repository.VerifiedEmailRepository;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final VerifiedEmailCodeRepository verifiedEmailCodeRepository;
    @Value("${server.server-url}")
    private String serverUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final long PASSWORD_CODE_TOKEN_TTL = 300L;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    private final JavaMailSender mailSender;

    private final ResetCodeGenerator resetCodeGenerator;

    public void sendEmailForPassword(PasswordResetCodeRequest PasswordResetCodeRequest) {
        if(!userRepository.existsByEmail((PasswordResetCodeRequest.email()))) {
            throw new AuthExceptionHandler(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        String code = resetCodeGenerator.generate();

        PasswordResetCode passwordResetCode = PasswordResetCode.create(
                PasswordResetCodeRequest.email(),
                code,
                PASSWORD_CODE_TOKEN_TTL
        );

        passwordResetCodeRepository.save(passwordResetCode);

        sendPasswordEmail(PasswordResetCodeRequest.email(), code);
    }

    public void verifyPasswordResetCode(PasswordCodeVerificationRequest passwordCodeVerificationRequest) {
        PasswordResetCode codeEntity = passwordResetCodeRepository.findById(passwordCodeVerificationRequest.email())
                .orElseThrow(() -> new AuthExceptionHandler(AuthErrorCode.VERIFICATION_NOT_FOUND));

        codeEntity.validateCode(passwordCodeVerificationRequest.code());

        verifiedEmailCodeRepository.save(VerifiedEmailCode.create(codeEntity.getEmail()));
    }

    public void checkEmailCodeVerification(String email) {
        verifiedEmailCodeRepository.findById(email).orElseThrow(
                () -> new AuthExceptionHandler(AuthErrorCode.CODE_NOT_VERIFIED)
        );
    }

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

    private void sendPasswordEmail(String email, String code) {
        String subject = "[STUDIO-X] 비밀번호 변경 인증 코드";
        String body = """
        안녕하세요, STUDIO-X입니다.
        
        비밀번호 변경을 위한 6자리 인증코드는 아래와 같습니다.
        인증 코드: %s
        
        본인이 요청한 인증이 아니라면, 이 메일은 무시하셔도 됩니다.
        해당 링크는 일정 시간이 지나면 자동으로 만료됩니다.
        """.formatted(code);
        sendMessage(email, subject, body);
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
