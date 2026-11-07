package OnlineBusBooking.Payment.payment_service.dto;

import jakarta.validation.constraints.NotBlank;

public record InitiateReq(@NotBlank String bookingRef) {}
