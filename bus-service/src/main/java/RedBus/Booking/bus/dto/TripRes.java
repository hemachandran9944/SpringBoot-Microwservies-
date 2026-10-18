package RedBus.Booking.bus.dto;

import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.BusType;
import lombok.Builder;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Builder
public record TripRes (
        Long id,
        Long busId,
        String busName,
        String operatorName,
        String contactEmail,
        String contactPhone,
        BusType busType, AcType acType,
        List <String> amenities,
        String source,
        String destination,
        LocalDate travelDate,
        LocalTime departureTime, LocalTime arrivalTime,
        long durationMinutes, BigDecimal fare,
        int availableSeats ){}

