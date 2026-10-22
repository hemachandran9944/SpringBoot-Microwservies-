package RedBus.Booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record SeatInfo(String seatNumber){}

