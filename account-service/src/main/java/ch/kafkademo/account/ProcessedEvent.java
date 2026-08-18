package ch.kafkademo.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProcessedEvent {
    @Id
    private String eventId; // The unique ID from the Kafka message (e.g., UUID)

    public ProcessedEvent() {}
    public ProcessedEvent(String eventId) { this.eventId = eventId; }

    // getters and setters omitted for brevity
}