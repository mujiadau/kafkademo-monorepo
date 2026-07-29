package ch.kafkademo.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    /**
     * Applies a delta to the customer's balance, creating the account on first
     * use.
     */
    @Transactional
    public Account applyTransaction(Long customerId, BigDecimal amount) {
        Account account = repository.findById(customerId)
                .orElseGet(() -> new Account(customerId, BigDecimal.ZERO));
        account.apply(amount);
        Account saved = repository.save(account);
        log.info("Updated balance for customer {} -> {}", customerId, saved.getBalance());
        return saved;
    }
}

