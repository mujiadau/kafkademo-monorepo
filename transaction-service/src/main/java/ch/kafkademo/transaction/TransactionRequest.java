package ch.kafkademo.transaction;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull Long customerId,
        @NotNull
        BigDecimal amount) {
}
