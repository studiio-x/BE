package net.studioxai.studioxBe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "billing_keys")
public class BillingKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_key_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String billingKey;

    @Column(nullable = true)
    private String method;

    @Column(nullable = true)
    private String bankName;

    @Column(nullable = true)
    private String bankAccountNumber;

    @Column(nullable = true)
    private String cardIssueCompany;

    @Column(nullable = true)
    private String cardAcquirerCompany;

    @Column(nullable = true)
    private String cardNumber;

    @Column(nullable = false)
    private boolean isActive;

    public static BillingKey create(
            User user,
            String billingKey,
            String method,
            String bankName,
            String bankAccountNumber,
            String cardIssueCompany,
            String cardAcquirerCompany,
            String cardNumber
    ) {
        return BillingKey.builder()
                .user(user)
                .billingKey(billingKey)
                .method(method)
                .bankName(bankName)
                .bankAccountNumber(bankAccountNumber)
                .cardIssueCompany(cardIssueCompany)
                .cardAcquirerCompany(cardAcquirerCompany)
                .cardNumber(cardNumber)
                .build();
    }

    @Builder
    private BillingKey(
            User user,
            String billingKey,
            String method,
            String bankName,
            String bankAccountNumber,
            String cardIssueCompany,
            String cardAcquirerCompany,
            String cardNumber
    ) {
        this.user = user;
        this.billingKey = billingKey;
        this.method = method;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
        this.cardIssueCompany = cardIssueCompany;
        this.cardAcquirerCompany = cardAcquirerCompany;
        this.cardNumber = cardNumber;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
