package net.studioxai.studioxBe.payment;

import net.studioxai.studioxBe.domain.payment.dto.CardDto;
import net.studioxai.studioxBe.domain.payment.dto.TransferDto;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyAuthKeyCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.request.BillingKeyCardCreateRequest;
import net.studioxai.studioxBe.domain.payment.dto.response.BillingKeyResponse;
import net.studioxai.studioxBe.domain.payment.entity.BillingKey;
import net.studioxai.studioxBe.domain.payment.exception.BillingKeyExceptionHandler;
import net.studioxai.studioxBe.domain.payment.repository.BillingKeyRepository;
import net.studioxai.studioxBe.domain.payment.service.BillingKeyService;
import net.studioxai.studioxBe.domain.payment.service.TossService;
import net.studioxai.studioxBe.domain.payment.util.JsonUtil;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private JsonUtil jsonUtil;

    @Mock
    private BillingKeyRepository billingKeyRepository;

    @InjectMocks
    private BillingKeyService billingKeyService;

    @Mock
    private User user;

    @Test
    @DisplayName("authKey로 빌링키 발급 성공 - 카드 정보 저장")
    void createBillingKeyWithAuthKey_success_card() throws IOException {
        // given
        Long userId = 1L;
        String customerKey = "customer-key";
        String billingKeyValue = "billing-key-123";

        BillingKeyAuthKeyCreateRequest request = Mockito.mock(BillingKeyAuthKeyCreateRequest.class);
        BillingKeyResponse response = Mockito.mock(BillingKeyResponse.class);
        CardDto card = Mockito.mock(CardDto.class);

        given(request.customerKey()).willReturn(customerKey);

        given(userService.getUserByIdOrThrow(userId))
                .willReturn(user);

        given(user.equalsCustomerKey(customerKey))
                .willReturn(true);

        given(tossService.getResponse(
                eq(request),
                eq(BillingKeyResponse.class),
                eq("/v1/billing/authorizations/issue")
        )).willReturn(response);

        given(response.billingKey()).willReturn(billingKeyValue);
        given(response.method()).willReturn("카드");
        given(response.card()).willReturn(card);
        given(response.transfers()).willReturn(null);

        given(card.issuerCode()).willReturn("61");
        given(card.acquirerCode()).willReturn("31");
        given(card.number()).willReturn("123456******7890");

        ArgumentCaptor<BillingKey> billingKeyCaptor =
                ArgumentCaptor.forClass(BillingKey.class);

        // when
        billingKeyService.createBillingKeyWithAuthKey(userId, request);

        // then
        verify(billingKeyRepository).save(billingKeyCaptor.capture());

        BillingKey savedBillingKey = billingKeyCaptor.getValue();

        assertThat(savedBillingKey.getUser()).isEqualTo(user);
        assertThat(savedBillingKey.getBillingKey()).isEqualTo(billingKeyValue);
        assertThat(savedBillingKey.getMethod()).isEqualTo("카드");

        assertThat(savedBillingKey.getCardIssueCompany()).isEqualTo("61");
        assertThat(savedBillingKey.getCardAcquirerCompany()).isEqualTo("31");
        assertThat(savedBillingKey.getCardNumber()).isEqualTo("123456******7890");

        assertThat(savedBillingKey.getBankName()).isNull();
        assertThat(savedBillingKey.getBankAccountNumber()).isNull();
    }

    @Test
    @DisplayName("카드 정보로 빌링키 발급 성공 - 계좌이체 정보 저장")
    void createBillingKeyWithCard_success_transfer() throws IOException {
        // given
        Long userId = 1L;
        String customerKey = "customer-key";
        String billingKeyValue = "billing-key-456";

        BillingKeyCardCreateRequest request = Mockito.mock(BillingKeyCardCreateRequest.class);
        BillingKeyResponse response = Mockito.mock(BillingKeyResponse.class);
        TransferDto transfer = Mockito.mock(TransferDto.class);

        given(request.customerKey()).willReturn(customerKey);

        given(userService.getUserByIdOrThrow(userId))
                .willReturn(user);

        given(user.equalsCustomerKey(customerKey))
                .willReturn(true);

        given(tossService.getResponse(
                eq(request),
                eq(BillingKeyResponse.class),
                eq("/v1/billing/authorizations/card")
        )).willReturn(response);

        given(response.billingKey()).willReturn(billingKeyValue);
        given(response.method()).willReturn("계좌이체");
        given(response.card()).willReturn(null);
        given(response.transfers()).willReturn(List.of(transfer));

        given(transfer.bankName()).willReturn("신한은행");
        given(transfer.bankAccountNumber()).willReturn("110123456789");

        ArgumentCaptor<BillingKey> billingKeyCaptor =
                ArgumentCaptor.forClass(BillingKey.class);

        // when
        billingKeyService.createBillingKeyWithCard(userId, request);

        // then
        verify(billingKeyRepository).save(billingKeyCaptor.capture());

        BillingKey savedBillingKey = billingKeyCaptor.getValue();

        assertThat(savedBillingKey.getUser()).isEqualTo(user);
        assertThat(savedBillingKey.getBillingKey()).isEqualTo(billingKeyValue);
        assertThat(savedBillingKey.getMethod()).isEqualTo("계좌이체");

        assertThat(savedBillingKey.getBankName()).isEqualTo("신한은행");
        assertThat(savedBillingKey.getBankAccountNumber()).isEqualTo("110123456789");

        assertThat(savedBillingKey.getCardIssueCompany()).isNull();
        assertThat(savedBillingKey.getCardAcquirerCompany()).isNull();
        assertThat(savedBillingKey.getCardNumber()).isNull();
    }

    @Test
    @DisplayName("authKey 빌링키 발급 실패 - customerKey 불일치")
    void createBillingKeyWithAuthKey_fail_invalidCustomerKey() throws IOException {
        // given
        Long userId = 1L;
        String requestCustomerKey = "wrong-customer-key";

        BillingKeyAuthKeyCreateRequest request = Mockito.mock(BillingKeyAuthKeyCreateRequest.class);

        given(request.customerKey()).willReturn(requestCustomerKey);

        given(userService.getUserByIdOrThrow(userId))
                .willReturn(user);

        given(user.equalsCustomerKey(requestCustomerKey))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                billingKeyService.createBillingKeyWithAuthKey(userId, request)
        ).isInstanceOf(BillingKeyExceptionHandler.class);

        verify(tossService, never())
                .getResponse(any(), any(), anyString());

        verify(billingKeyRepository, never())
                .save(any(BillingKey.class));
    }

    @Test
    @DisplayName("카드 빌링키 발급 실패 - customerKey 불일치")
    void createBillingKeyWithCard_fail_invalidCustomerKey() throws IOException {
        // given
        Long userId = 1L;
        String requestCustomerKey = "wrong-customer-key";

        BillingKeyCardCreateRequest request = Mockito.mock(BillingKeyCardCreateRequest.class);

        given(request.customerKey()).willReturn(requestCustomerKey);

        given(userService.getUserByIdOrThrow(userId))
                .willReturn(user);

        given(user.equalsCustomerKey(requestCustomerKey))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                billingKeyService.createBillingKeyWithCard(userId, request)
        ).isInstanceOf(BillingKeyExceptionHandler.class);

        verify(tossService, never())
                .getResponse(any(), any(), anyString());

        verify(billingKeyRepository, never())
                .save(any(BillingKey.class));
    }
}