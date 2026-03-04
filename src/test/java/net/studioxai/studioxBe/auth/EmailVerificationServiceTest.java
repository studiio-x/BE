package net.studioxai.studioxBe.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.studioxai.studioxBe.domain.auth.dto.request.EmailVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.PasswordCodeVerificationRequest;
import net.studioxai.studioxBe.domain.auth.dto.request.PasswordResetCodeRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.EmailValidationResponse;
import net.studioxai.studioxBe.domain.auth.entity.EmailVerificationToken;
import net.studioxai.studioxBe.domain.auth.entity.PasswordResetCode;
import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmail;
import net.studioxai.studioxBe.domain.auth.entity.VerifiedEmailCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.auth.repository.EmailVerificationTokenRepository;
import net.studioxai.studioxBe.domain.auth.repository.PasswordResetCodeRepository;
import net.studioxai.studioxBe.domain.auth.repository.VerifiedEmailCodeRepository;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.auth.util.ResetCodeGenerator;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.auth.repository.VerifiedEmailRepository;
import net.studioxai.studioxBe.domain.auth.service.EmailVerificationService;
import net.studioxai.studioxBe.global.jwt.JwtProperties;
import net.studioxai.studioxBe.global.jwt.JwtProvider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {
    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private VerifiedEmailRepository verifiedEmailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private VerifiedEmailCodeRepository verifiedEmailCodeRepository;

    @Mock
    private ResetCodeGenerator resetCodeGenerator;

    @BeforeEach
    void init() {
        // @Value 값 설정
        ReflectionTestUtils.setField(emailVerificationService, "serverUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailVerificationService, "senderEmail", "noreply@studiox.com");
    }

    @Test
    @DisplayName("sendEmailForPassword: 이메일이 없으면 EMAIL_NOT_FOUND 예외")
    void sendEmailForPassword_emailNotFound_throw() {
        // given
        String email = "noone@test.com";
        PasswordResetCodeRequest req = new PasswordResetCodeRequest(email);

        Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        AuthExceptionHandler ex = assertThrows(AuthExceptionHandler.class,
                () -> emailVerificationService.sendEmailForPassword(req));

        // then
        assertEquals(AuthErrorCode.EMAIL_NOT_FOUND, ex.getErrorCode());
        Mockito.verify(passwordResetCodeRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("sendEmailForPassword: 정상 - 코드 저장 + 메일 발송(6자리 0패딩)")
    void sendEmailForPassword_success_saveAndSendMail() {
        // given
        String email = "user@test.com";
        PasswordResetCodeRequest req = new PasswordResetCodeRequest(email);

        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

        Mockito.when(resetCodeGenerator.generate()).thenReturn("000042");

        Mockito.when(passwordResetCodeRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);

        // when
        emailVerificationService.sendEmailForPassword(req);

        // then
        Mockito.verify(passwordResetCodeRepository).save(captor.capture());
        PasswordResetCode saved = captor.getValue();

        assertEquals(email, saved.getEmail());
        assertEquals("000042", saved.getCode());
    }

    @Test
    @DisplayName("verifyPasswordResetCode: 저장된 코드가 없으면 VERIFICATION_NOT_FOUND 예외")
    void verifyPasswordResetCode_notFound_throw() {
        // given
        String email = "user@test.com";
        PasswordCodeVerificationRequest req = new PasswordCodeVerificationRequest(email, "123456");

        Mockito.when(passwordResetCodeRepository.findById(email)).thenReturn(Optional.empty());

        // when
        AuthExceptionHandler ex = assertThrows(AuthExceptionHandler.class,
                () -> emailVerificationService.verifyPasswordResetCode(req));

        // then
        assertEquals(AuthErrorCode.VERIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("verifyPasswordResetCode: 정상 - 코드 검증 후 VerifiedEmailCode 저장")
    void verifyPasswordResetCode_success_saveVerified() {
        // given
        String email = "user@test.com";
        String code = "123456";
        PasswordCodeVerificationRequest req = new PasswordCodeVerificationRequest(email, code);

        PasswordResetCode codeEntity = Mockito.mock(PasswordResetCode.class);
        Mockito.when(codeEntity.getEmail()).thenReturn(email);

        Mockito.when(passwordResetCodeRepository.findById(email)).thenReturn(Optional.of(codeEntity));
        Mockito.when(verifiedEmailCodeRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        assertDoesNotThrow(() -> emailVerificationService.verifyPasswordResetCode(req));

        // then
        Mockito.verify(codeEntity).validateCode(code);

        ArgumentCaptor<VerifiedEmailCode> captor = ArgumentCaptor.forClass(VerifiedEmailCode.class);
        Mockito.verify(verifiedEmailCodeRepository).save(captor.capture());

        VerifiedEmailCode saved = captor.getValue();
        assertEquals(email, saved.getEmail());
    }

    @Test
    @DisplayName("checkEmailCodeVerification: 인증 기록이 없으면 CODE_NOT_VERIFIED 예외")
    void checkEmailCodeVerification_notVerified_throw() {
        // given
        String email = "user@test.com";
        Mockito.when(verifiedEmailCodeRepository.findById(email)).thenReturn(Optional.empty());

        // when
        AuthExceptionHandler ex = assertThrows(AuthExceptionHandler.class,
                () -> emailVerificationService.checkEmailCodeVerification(email));

        // then
        assertEquals(AuthErrorCode.CODE_NOT_VERIFIED, ex.getErrorCode());
    }

    @Test
    @DisplayName("checkEmailCodeVerification: 인증 기록이 있으면 통과")
    void checkEmailCodeVerification_verified_ok() {
        // given
        String email = "user@test.com";
        VerifiedEmailCode verified = Mockito.mock(VerifiedEmailCode.class);

        Mockito.when(verifiedEmailCodeRepository.findById(email)).thenReturn(Optional.of(verified));

        // when & then
        assertDoesNotThrow(() -> emailVerificationService.checkEmailCodeVerification(email));
    }

    @Test
    @DisplayName("checkEmailVerification 성공 - 인증된 이메일 존재하면 통과")
    void checkEmailVerification_success() {
        String email = "test@example.com";
        Mockito.when(verifiedEmailRepository.findById(email))
                .thenReturn(Optional.of(VerifiedEmail.create(email)));

        emailVerificationService.checkEmailVerification(email);

        Mockito.verify(verifiedEmailRepository).findById(email);
    }

    @Test
    @DisplayName("checkEmailVerification 실패 - 인증된 이메일 없으면 예외")
    void checkEmailVerification_fail_notFound() {
        String email = "nope@example.com";
        Mockito.when(verifiedEmailRepository.findById(email))
                .thenReturn(Optional.empty());

        AuthExceptionHandler ex = assertThrows(
                AuthExceptionHandler.class,
                () -> emailVerificationService.checkEmailVerification(email)
        );

        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("sendEmail 실패 - 이미 가입된 이메일이면 예외")
    void sendEmail_fail_duplicateUser() {
        EmailVerificationRequest req =
                new EmailVerificationRequest("exist@example.com", "/callback");

        Mockito.when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(Mockito.mock()));

        AuthExceptionHandler ex = assertThrows(
                AuthExceptionHandler.class,
                () -> emailVerificationService.sendEmail(req, "/email/verify")
        );

        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_REGISTERS);
    }

    @Test
    @DisplayName("sendEmail 성공 - 토큰 생성 / 저장 / 메일 전송")
    void sendEmail_success() throws MessagingException {

        EmailVerificationRequest req =
                new EmailVerificationRequest("new@example.com", "/callback");

        Mockito.when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        Mockito.when(jwtProvider.createEmailToken(req.email())).thenReturn("mail-token");
        Mockito.when(jwtProperties.mailTokenExpirationMs()).thenReturn(600000L);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailVerificationService.sendEmail(req, "/email/verify");

        Mockito.verify(tokenRepository).save(tokenCaptor.capture());
        EmailVerificationToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getEmail()).isEqualTo(req.email());
        assertThat(savedToken.getToken()).isEqualTo("mail-token");
        assertThat(savedToken.getCallbackUrl()).isEqualTo(req.callbackUrl());

        Mockito.verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("verifyEmail 성공 - 토큰 검증 후 VerifiedEmail 저장")
    void verifyEmail_success() {
        String email = "auth@example.com";
        String token = "valid-token";
        String callbackUrl = "/signup";

        EmailVerificationToken savedToken =
                EmailVerificationToken.create(email, token, callbackUrl, 600000L);

        Mockito.when(tokenRepository.findById(email))
                .thenReturn(Optional.of(savedToken));

        String result = emailVerificationService.verifyEmail(email, token);

        assertThat(result).isEqualTo(callbackUrl);
        Mockito.verify(verifiedEmailRepository).save(ArgumentMatchers.any(VerifiedEmail.class));
    }

    @Test
    @DisplayName("verifyEmail 실패 - 토큰 레코드 없음")
    void verifyEmail_fail_notFound() {
        String email = "unknown@example.com";

        Mockito.when(tokenRepository.findById(email)).thenReturn(Optional.empty());

        AuthExceptionHandler ex = assertThrows(
                AuthExceptionHandler.class,
                () -> emailVerificationService.verifyEmail(email, "any-token")
        );

        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.VERIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("verifyEmail 실패 - 토큰 불일치")
    void verifyEmail_fail_wrongToken() {
        String email = "auth@example.com";
        String wrongToken = "wrong";

        EmailVerificationToken savedToken =
                EmailVerificationToken.create(email, "real-token", "/callback", 600000L);

        Mockito.when(tokenRepository.findById(email))
                .thenReturn(Optional.of(savedToken));

        AuthExceptionHandler ex = assertThrows(
                AuthExceptionHandler.class,
                () -> emailVerificationService.verifyEmail(email, wrongToken)
        );

        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_EMAIL_TOKEN);
    }

    @Test
    @DisplayName("이메일이 검증된 이메일 테이블에 존재하면 isAvailable = true")
    void getEmailValidation_whenEmailExists_thenAvailableTrue() {
        // given
        String email = "test@example.com";
        VerifiedEmail verifiedEmail = VerifiedEmail.create(email);

        given(verifiedEmailRepository.findById(email))
                .willReturn(Optional.of(verifiedEmail));

        // when
        EmailValidationResponse response =
                emailVerificationService.getEmailValidation(email);

        // then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.isVerified()).isTrue();

        then(verifiedEmailRepository).should().findById(email);
    }

    @Test
    @DisplayName("이메일이 검증된 이메일 테이블에 없으면 isAvailable = false")
    void getEmailValidation_whenEmailNotExists_thenAvailableFalse() {
        // given
        String email = "not-exist@example.com";

        given(verifiedEmailRepository.findById(email))
                .willReturn(Optional.empty());

        // when
        EmailValidationResponse response =
                emailVerificationService.getEmailValidation(email);

        // then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.isVerified()).isFalse();

        then(verifiedEmailRepository).should().findById(email);
    }
}
