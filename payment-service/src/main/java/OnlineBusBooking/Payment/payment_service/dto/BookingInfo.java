package OnlineBusBooking.Payment.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingInfo(
        String bookingRef, String status, BigDecimal totalAmount, LocalDateTime expiresAt,
        LocalDate travelDate, LocalTime departureTime) {}
