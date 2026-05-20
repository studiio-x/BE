package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.dto.CardDto;
import net.studioxai.studioxBe.domain.payment.dto.TransferDto;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyAuthKeyCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyCardCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.BillingKeyResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.entity.enums.Plan;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyApprovalService;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyService;
import net.studioxai.studioxBe.domain.payment.service.TossService;
import net.studioxai.studioxBe.domain.payment.util.JsonUtil;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingKeyServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TossService tossService;

    @Mock
    private BillingKeyApprovalService billingKeyApprovalService;

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private BillingKeyRepository billingKeyRepository;

    @InjectMocks
    private BillingKeyService billingKeyService;

    private static final Long USER_ID = 1L;
    private static final String CUSTOMER_KEY = "customer-key-123";
    private static final String CLIENT_IP = "127.0.0.1";

    @Test
    @DisplayName("AuthKey로 빌링키 발급 성공 - 카드 정보 저장 후 결제 승인")
    void createBillingKeyWithAuthKey_success_card() throws IOException {
        // given
        User user = Mockito.mock(User.class);
        BillingKeyAuthKeyCreateRequest request = Mockito.mock(BillingKeyAuthKeyCreateRequest.class);

        CardDto card = Mockito.mock(CardDto.class);
        given(card.issuerCode()).willReturn("61");
        given(card.acquirerCode()).willReturn("31");
        given(card.number()).willReturn("12345678****1234");

        BillingKeyResponse response = Mockito.mock(BillingKeyResponse.class);
        given(response.billingKey()).willReturn("billing-key-123");
        given(response.method()).willReturn("카드");
        given(response.card()).willReturn(card);
        given(response.transfers()).willReturn(null);

        given(request.customerKey()).willReturn(CUSTOMER_KEY);
        given(user.equalsCustomerKey(CUSTOMER_KEY)).willReturn(true);
        given(userService.getUserByIdOrThrow(USER_ID)).willReturn(user);

        given(tossService.getResponse(
                eq(request),
                eq(BillingKeyResponse.class),
                eq("/v1/billing/authorizations/issue")
        )).willReturn(response);

        Plan plan = Plan.values()[0];

        // when
        billingKeyService.createBillingKeyWithAuthKey(
                USER_ID,
                request,
                plan,
                CLIENT_IP
        );

        // then
        ArgumentCaptor<BillingKey> captor = ArgumentCaptor.forClass(BillingKey.class);
        BDDMockito.then(billingKeyRepository).should().save(captor.capture());

        BillingKey savedBillingKey = captor.getValue();

        assertThat(savedBillingKey.getUser()).isEqualTo(user);
        assertThat(savedBillingKey.getBillingKey()).isEqualTo("billing-key-123");
        assertThat(savedBillingKey.getMethod()).isEqualTo("카드");
        assertThat(savedBillingKey.getCardIssueCompany()).isEqualTo("61");
        assertThat(savedBillingKey.getCardAcquirerCompany()).isEqualTo("31");
        assertThat(savedBillingKey.getCardNumber()).isEqualTo("12345678****1234");
        assertThat(savedBillingKey.getBankName()).isNull();
        assertThat(savedBillingKey.getBankAccountNumber()).isNull();

        BDDMockito.then(billingKeyApprovalService)
                .should()
                .approveBilling(user, plan, CLIENT_IP);
    }

    @Test
    @DisplayName("Card 정보로 빌링키 발급 성공 - 계좌이체 정보 저장 후 결제 승인")
    void createBillingKeyWithCard_success_transfer() throws IOException {
        // given
        User user = Mockito.mock(User.class);
        BillingKeyCardCreateRequest request = Mockito.mock(BillingKeyCardCreateRequest.class);

        TransferDto transfer = Mockito.mock(TransferDto.class);
        given(transfer.bankName()).willReturn("국민은행");
        given(transfer.bankAccountNumber()).willReturn("1234567890");

        BillingKeyResponse response = Mockito.mock(BillingKeyResponse.class);
        given(response.billingKey()).willReturn("billing-key-transfer");
        given(response.method()).willReturn("계좌이체");
        given(response.card()).willReturn(null);
        given(response.transfers()).willReturn(List.of(transfer));

        given(request.customerKey()).willReturn(CUSTOMER_KEY);
        given(user.equalsCustomerKey(CUSTOMER_KEY)).willReturn(true);
        given(userService.getUserByIdOrThrow(USER_ID)).willReturn(user);

        given(tossService.getResponse(
                eq(request),
                eq(BillingKeyResponse.class),
                eq("/v1/billing/authorizations/card")
        )).willReturn(response);

        Plan plan = Plan.values()[0];

        // when
        billingKeyService.createBillingKeyWithCard(
                USER_ID,
                request,
                plan,
                CLIENT_IP
        );

        // then
        ArgumentCaptor<BillingKey> captor = ArgumentCaptor.forClass(BillingKey.class);
        BDDMockito.then(billingKeyRepository).should().save(captor.capture());

        BillingKey savedBillingKey = captor.getValue();

        assertThat(savedBillingKey.getUser()).isEqualTo(user);
        assertThat(savedBillingKey.getBillingKey()).isEqualTo("billing-key-transfer");
        assertThat(savedBillingKey.getMethod()).isEqualTo("계좌이체");
        assertThat(savedBillingKey.getBankName()).isEqualTo("국민은행");
        assertThat(savedBillingKey.getBankAccountNumber()).isEqualTo("1234567890");
        assertThat(savedBillingKey.getCardIssueCompany()).isNull();
        assertThat(savedBillingKey.getCardAcquirerCompany()).isNull();
        assertThat(savedBillingKey.getCardNumber()).isNull();

        BDDMockito.then(billingKeyApprovalService)
                .should()
                .approveBilling(user, plan, CLIENT_IP);
    }

    @Test
    @DisplayName("AuthKey 빌링키 발급 실패 - customerKey가 일치하지 않으면 예외 발생")
    void createBillingKeyWithAuthKey_fail_invalidCustomerKey() {
        // given
        User user = Mockito.mock(User.class);
        BillingKeyAuthKeyCreateRequest request = Mockito.mock(BillingKeyAuthKeyCreateRequest.class);

        given(request.customerKey()).willReturn("wrong-customer-key");
        given(user.equalsCustomerKey("wrong-customer-key")).willReturn(false);
        given(userService.getUserByIdOrThrow(USER_ID)).willReturn(user);

        Plan plan = Plan.values()[0];

        // when & then
        assertThatThrownBy(() ->
                billingKeyService.createBillingKeyWithAuthKey(
                        USER_ID,
                        request,
                        plan,
                        CLIENT_IP
                )
        ).isInstanceOf(BillingKeyExceptionHandler.class);

        BDDMockito.then(tossService).shouldHaveNoInteractions();
        BDDMockito.then(billingKeyRepository).shouldHaveNoInteractions();
        BDDMockito.then(billingKeyApprovalService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Card 빌링키 발급 실패 - customerKey가 일치하지 않으면 예외 발생")
    void createBillingKeyWithCard_fail_invalidCustomerKey() {
        // given
        User user = Mockito.mock(User.class);
        BillingKeyCardCreateRequest request = Mockito.mock(BillingKeyCardCreateRequest.class);

        given(request.customerKey()).willReturn("wrong-customer-key");
        given(user.equalsCustomerKey("wrong-customer-key")).willReturn(false);
        given(userService.getUserByIdOrThrow(USER_ID)).willReturn(user);

        Plan plan = Plan.values()[0];

        // when & then
        assertThatThrownBy(() ->
                billingKeyService.createBillingKeyWithCard(
                        USER_ID,
                        request,
                        plan,
                        CLIENT_IP
                )
        ).isInstanceOf(BillingKeyExceptionHandler.class);

        BDDMockito.then(tossService).shouldHaveNoInteractions();
        BDDMockito.then(billingKeyRepository).shouldHaveNoInteractions();
        BDDMockito.then(billingKeyApprovalService).shouldHaveNoInteractions();
    }
}