package RedBus.Booking.bus.service;

import RedBus.Booking.bus.dto.TripReq;
import RedBus.Booking.bus.dto.TripRes;
import RedBus.Booking.bus.model.*;
import RedBus.Booking.bus.repository.BusRepository;
import RedBus.Booking.bus.repository.TripRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TripService{
    private final TripRepository tripRepository;
    private final BusRepository busRepository;


    @Transactional
    public TripRes createTrip(TripReq req) {
        Bus bus = busRepository.findById(req.busId())
                .orElseThrow(() -> new NotFoundException("Bus not found: " + req.busId()));

        if (bus.getStatus() != BusStatus.ACTIVE){
            throw new IllegalArgumentException("Bus is not ACTIVE");
        }

        if (req.source().equalsIgnoreCase(req.destination())){
            throw new IllegalArgumentException("Source and destination cannot be same");
        }

        Trip trip = Trip.builder()
                .bus(bus).source(req.source().trim()).destination(req.destination().trim())
                .travelDate(req.travelDate()).departureTime(req.departureTime())
                .arrivalTime(req.arrivalTime()).fare(req.fare())
                .availableSeats(bus.getTotalSeats()).build();
        return toResponse(tripRepository.save(trip));
    }



    @Transactional(readOnly = true)
    public Page<TripRes> search (String from, String to, LocalDate data,
                                 BusType busType, AcType acType, BigDecimal maxFare,
                                 LocalTime departAfter, LocalTime departBefore,
                                 String sortBy, int page, int size
                                 ){
        Sort sort = switch (sortBy == null ? "departure" : sortBy.toLowerCase()){
            case "fare" -> Sort.by("fare").ascending();
            case "fare_desc"-> Sort.by("fare").descending();
            case "seats"-> Sort.by("availableSeats").descending();
            default -> Sort.by("departureTime").ascending();
        };
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), sort);
        return tripRepository.search(from.trim(), to.trim(), data, busType, acType,
                maxFare, departAfter, departBefore, pageable).map(this::toResponse);

    }



    @Transactional(readOnly = true)
    public TripRes get(Long id) {
        return toResponse(tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found: " + id)));
    }










    /* Trip Respones Helper */
    TripRes toResponse(Trip trip){
        Bus bus = trip.getBus();

        long minutes = Duration.between(trip.getDepartureTime(), trip.getArrivalTime()).toMinutes();
        if (minutes <= 0){
            minutes += 24*60;
        }

        return new TripRes(trip.getId(), bus.getId(), bus.getBusName(),
                bus.getOperator().getName(), bus.getOperator().getContactEmail(), bus.getOperator().getContactPhone(),
                bus.getBusType(), bus.getAcType(),
                bus.getAmenities().stream().map(Amenity::getName).sorted().toList(),
                trip.getSource(), trip.getDestination(), trip.getTravelDate(),
                trip.getDepartureTime(), trip.getArrivalTime(), minutes,
                trip.getFare(), trip.getAvailableSeats());

    }

}
