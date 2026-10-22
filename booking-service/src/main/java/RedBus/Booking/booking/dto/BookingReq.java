package RedBus.Booking.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookingReq(
        @NotNull Long tripId,
        @NotEmpty @Size(max = 6, message = "Maximum 6 seats per booking") @Valid List<PassengerReq> passengers) {}

