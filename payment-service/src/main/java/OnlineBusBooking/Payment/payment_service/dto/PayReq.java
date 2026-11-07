package OnlineBusBooking.Payment.payment_service.dto;

import OnlineBusBooking.Payment.payment_service.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayReq(@NotNull PaymentMethod method, Boolean simulateFailure) {}
