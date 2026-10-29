package RedBus.Booking.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PassengerReq(
        @NotBlank String seatNumber,
        @NotBlank String passengerName,
        @Min(1) @Max(120) String age,
        @NotBlank @Pattern(regexp = "(?i)^(MALE|FEMALE|OTHER)$", message = "MALE / FEMALE / OTHER") String gender) {}
