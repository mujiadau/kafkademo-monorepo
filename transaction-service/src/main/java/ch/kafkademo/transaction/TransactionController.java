package ch.kafkademo.transaction;

import ch.kafkademo.common.TransactionEvent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionProducer producer;
    public TransactionController(TransactionProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<TransactionEvent> create(@Valid @RequestBody TransactionRequest request) {
        TransactionEvent event = new TransactionEvent(
                request.customerId(),
                request.amount(),
                "CHF",
                System.currentTimeMillis()
        );
        producer.publish(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);

    }
}
