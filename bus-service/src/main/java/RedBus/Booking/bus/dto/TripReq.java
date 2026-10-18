package RedBus.Booking.bus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TripReq (
        @NotNull  Long  busId,
        @NotBlank String source,
        @NotBlank String destination,
        @NotNull  @FutureOrPresent LocalDate travelDate,
        @NotNull LocalTime departureTime,
        @NotNull LocalTime arrivalTime,
        @NotNull @DecimalMin("1.0") BigDecimal fare) {}

