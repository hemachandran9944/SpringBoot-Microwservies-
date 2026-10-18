package RedBus.Booking.bus.dto;

import jakarta.validation.constraints.NotBlank;

public record AmenityReq(@NotBlank String name){
}
