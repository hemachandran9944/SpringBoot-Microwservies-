package RedBus.Booking.bus.dto;

import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.BusStatus;
import RedBus.Booking.bus.model.BusType;

import java.util.List;

public record BusRes (
        Long id,
        String busName,
        String registrationNumber,
        String operatorName,
        String contactPhone,
        String contactEmail,
        BusType busType,
        AcType acType,
        int totalSeats,
        BusStatus status,
        List<String> amenities){}

