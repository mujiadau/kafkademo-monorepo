package ch.kafkademo.account;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    protected Account() {
    }

    public Account(Long customerId, BigDecimal balance) {
        this.customerId = customerId;
        this.balance = balance;
    }

    public void apply(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
