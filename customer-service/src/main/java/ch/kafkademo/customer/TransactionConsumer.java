package ch.kafkademo.customer;

import ch.kafkademo.common.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final EmailService emailService;

    public TransactionConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "${kafkademo.topic.transactions}",
            groupId = "customer-service")
    public void onTransaction(TransactionEvent event) {
        log.info("Received transaction event: {}", event);
        emailService.notifyBalanceChange(event);
    }
}