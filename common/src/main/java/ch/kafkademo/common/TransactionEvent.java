package ch.kafkademo.common;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionEvent {
    private Long customerId;
    private BigDecimal amount;
    private String currency = "CHF";
    private long timestamp = System.currentTimeMillis();
    private UUID eventId = UUID.randomUUID();

    public TransactionEvent() {

    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getEventId() {return this.eventId; }

    public TransactionEvent(Long customerId, BigDecimal amount, String currency, long timestamp) {
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "customerId=" + customerId +
                ",amount=" + amount +
                ",currency=" + currency +
                ",eventId=" + eventId +
                ",timestamp=" + timestamp + '}';
    }
}
