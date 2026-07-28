package ch.kafkademo.transaction;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull Long customerID,
        @NotNull
        BigDecimal amount) {
}
