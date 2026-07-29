package ch.kafkademo.account;

import ch.kafkademo.common.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final AccountService accountService;

    public TransactionConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(
            topics = "${pgbank.topic.transactions}",
            groupId = "account-service")
    public void onTransaction(TransactionEvent event) {
        log.info("Received transaction event: {}", event);
        accountService.applyTransaction(event.getCustomerId(), event.getAmount());
    }
}