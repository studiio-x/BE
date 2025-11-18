package net.studioxai.studioxBe.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.user.dto.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.user.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.user.repository.EmailVerificationTokenRepository;
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
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final JavaMailSender mailSender;

    public void sendEmail(EmailVerificationRequest emailVerificationRequest, String currentUrl) {
        // TODO: 이미 DB에 등록된 메일은 아닌지 확인

        // TODO: 토큰 생성 및 레디스 저장 책임 분리 필요
        String token = jwtProvider.createEmailToken(emailVerificationRequest.email());

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.create(
                emailVerificationRequest.email(),
                token,
                emailVerificationRequest.callbackUrl(),
                jwtProperties.mailTokenExpirationMs()
        );

        emailVerificationTokenRepository.save(emailVerificationToken);

        String url = serverUrl + currentUrl + "?token=" + token;

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
            throw new IllegalStateException("이메일 전송에 실패했습니다.", e);
        }

    }

}
