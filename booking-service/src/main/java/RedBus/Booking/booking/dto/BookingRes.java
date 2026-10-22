package RedBus.Booking.booking.dto;

import RedBus.Booking.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BookingRes(
        String bookingRef, BookingStatus status, Long tripId, String busName,
        String source, String destination, LocalDate travelDate, LocalTime departureTime,
        BigDecimal totalAmount, LocalDateTime expiresAt, LocalDateTime createdAt,
        List<PassengerRes> passengers) {}
