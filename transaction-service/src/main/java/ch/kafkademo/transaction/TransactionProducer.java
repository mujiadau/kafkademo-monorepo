package ch.kafkademo.transaction;

import ch.kafkademo.common.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionProducer {
    private static final Logger log = LoggerFactory.getLogger(TransactionProducer.class);

    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final String topic;
    public TransactionProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate,
                               @Value("${kafkademo.topic.transactions}")String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(TransactionEvent event) {
        log.info("Publishing transaction event to kafka topic: '{}':{}", topic, event);

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(topic, String.valueOf(event.getCustomerId()), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // Success: Log at debug or trace to avoid flooding your logs
                log.debug("Successfully sent transaction {} to partition {} with offset {}",
                        event.getCustomerId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                // Failure: Log the error.
                log.error("Failed to send transaction {} to topic {} due to: {}",
                        event.getCustomerId(), topic, ex.getMessage(), ex);
            }
        });
    }


}
