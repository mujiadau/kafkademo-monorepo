package ch.kafkademo.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final ProcessedEventRepository eventRepository;

    public AccountService(AccountRepository accountRepository, ProcessedEventRepository eventRepository) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
    }
    /**
     * Applies a delta to the customer's balance idempotently.
     *
     * @param customerId The customer to update.
     * @param amount     The amount to apply.
     */
    @Transactional
    public Account applyTransaction(Long customerId, BigDecimal amount, UUID eventId) {
        // 1. Idempotency Check
        if (eventRepository.existsById(eventId.toString())) {
            log.warn("Event {} was already processed. Skipping to prevent duplicate transaction.", eventId);
            return accountRepository.findById(customerId).orElse(null);
        }

        // 2. Mark event as processed
        // (If two threads process the exact same event simultaneously,
        // one will throw a DataIntegrityViolationException due to the @Id constraint)
        eventRepository.save(new ProcessedEvent(eventId.toString()));

        // 3. Apply business logic
        Account account = accountRepository.findById(customerId)
                .orElseGet(() -> new Account(customerId, BigDecimal.ZERO));

        account.apply(amount);
        Account saved = accountRepository.save(account);

        log.info("Updated balance for customer {} -> {} (Event: {})", customerId, saved.getBalance(), eventId);
        return saved;
    }
}

