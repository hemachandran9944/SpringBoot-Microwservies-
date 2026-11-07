package OnlineBusBooking.Payment.payment_service.dto;

import OnlineBusBooking.Payment.payment_service.model.PaymentMethod;
import OnlineBusBooking.Payment.payment_service.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRes(
        String paymentRef, String bookingRef, BigDecimal amount, String currency,
        PaymentStatus status, PaymentMethod method, String gatewayTxnId, String failureReason,
        BigDecimal refundAmount, LocalDateTime createdAt, LocalDateTime paidAt, LocalDateTime refundedAt) {}
