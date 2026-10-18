package RedBus.Booking.bus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OperatorReq(
        @NotBlank String name,
        @NotBlank String licenseNumber,
        @Email String contactEmail,
        String contactPhone){}


