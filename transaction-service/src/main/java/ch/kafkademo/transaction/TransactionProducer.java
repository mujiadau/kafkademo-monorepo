package ch.kafkademo.transaction;

import ch.kafkademo.common.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
        kafkaTemplate.send(topic, String.valueOf(event.getCustomerId()), event);
    }


}
