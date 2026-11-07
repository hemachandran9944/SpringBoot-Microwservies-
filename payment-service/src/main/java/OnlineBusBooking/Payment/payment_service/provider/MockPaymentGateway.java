package OnlineBusBooking.Payment.payment_service.provider;

import OnlineBusBooking.Payment.payment_service.model.Payment;
import OnlineBusBooking.Payment.payment_service.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {


    public GatewayResult charge(Payment payment, PaymentMethod method, boolean simulateFailure) {
        if (simulateFailure) {
            log.info("MOCK charge FAILED ref={}", payment.getPaymentRef());
            return GatewayResult.failed("Payment declined by bank (simulated)");
        }
        String txn = "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("MOCK charge OK ref={} method={} txn={}", payment.getPaymentRef(), method, txn);
        return GatewayResult.ok(txn);
    }


    public GatewayResult refund(Payment payment, BigDecimal amount) {
        String txn = "MOCK-RF-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        log.info("MOCK refund OK ref={} amount={} txn={}", payment.getPaymentRef(), amount, txn);
        return GatewayResult.ok(txn);
    }
}
