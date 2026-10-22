package RedBus.Booking.booking.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiEnvelope <T> (String status, String message, T data){}
