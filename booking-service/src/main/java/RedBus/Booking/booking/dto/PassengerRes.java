package RedBus.Booking.booking.dto;

import java.math.BigDecimal;

public record PassengerRes(String seatNumber, String passengerName, String age, String gender, BigDecimal fare) {}
