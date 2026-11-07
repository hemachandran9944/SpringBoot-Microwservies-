package OnlineBusBooking.Payment.payment_service.provider;

import OnlineBusBooking.Payment.payment_service.model.Payment;
import OnlineBusBooking.Payment.payment_service.model.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {

    /* simulateFailure: mock gateway only use ,real gateway ignore */
    GatewayResult charge(Payment payment, PaymentMethod method, boolean simulateFailure);
    GatewayResult refund(Payment payment, BigDecimal amount);


}
