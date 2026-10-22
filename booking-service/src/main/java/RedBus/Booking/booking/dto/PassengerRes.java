package RedBus.Booking.booking.dto;

import java.math.BigDecimal;

public record PassengerRes (String seatNumber, String passengerName, int age, String gender, BigDecimal fare){
}
