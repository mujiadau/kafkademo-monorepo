package ch.kafkademo.customer;

import ch.kafkademo.common.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stand-in for a real email provider. Instead of sending an email it simply
 * logs a line such as {@code sent email: +20 CHF}.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public String notifyBalanceChange(TransactionEvent event) {
        String message = String.format("sent email: %s%s %s",
                event.getAmount().signum() >= 0 ? "+" : "",
                event.getAmount().stripTrailingZeros().toPlainString(),
                event.getCurrency());
        log.info("[customer {}] {}", event.getCustomerId(), message);
        return message;
    }
}
