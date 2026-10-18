package RedBus.Booking.bus.dto;

import jakarta.validation.constraints.NotBlank;

public record SeatReq(@NotBlank String seatNumber, int seatRow, int seatColumn, String deck, String seatType) {}

