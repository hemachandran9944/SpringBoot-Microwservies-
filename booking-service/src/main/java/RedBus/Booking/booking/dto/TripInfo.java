package RedBus.Booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@JsonIgnoreProperties(ignoreUnknown = true)
public record TripInfo(
        Long tripId, Long busId, String busName, String source, String destination,
        LocalDate travelDate, LocalTime departureTime, BigDecimal fare, Integer availableSeats) {}









