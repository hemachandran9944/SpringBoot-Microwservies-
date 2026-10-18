package RedBus.Booking.bus.dto;

import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.BusType;
import jakarta.validation.constraints.*;

import java.util.Set;

public record BusReq(
        @NotBlank String busName,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$",
                message = "Format: TN01AB1234") String registrationNumber,
        @NotNull Long operatorId,
        @NotNull BusType busType,
        @NotNull AcType acType,
        @Min(1) @Max(80) int totalSeats,
        Set<Long> amenityIds){}


