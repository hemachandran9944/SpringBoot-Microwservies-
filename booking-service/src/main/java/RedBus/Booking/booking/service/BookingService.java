package RedBus.Booking.booking.service;


import RedBus.Booking.booking.client.BusClient;
import RedBus.Booking.booking.dto.*;
import RedBus.Booking.booking.exception.ConflictException;
import RedBus.Booking.booking.model.*;
import RedBus.Booking.booking.repository.BookingRepository;
import RedBus.Booking.booking.repository.TripSeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripSeatRepository tripSeatRepository;
    private final BusClient busClient;

    @Value("${booking.lock-minutes:10}")
    private int lockMinutes;


    @Transactional
    public BookingRes createBooking(BookingReq req, String userEmail){

            /* Bus trip departed find out */

        TripInfo trip = busClient.getTrip(req.tripId());
        LocalDateTime departure = LocalDateTime.of(trip.travelDate(), trip.departureTime());
        if (departure.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("This trip has already departed");
        }

        /*Bus duplicate seat find throw exception */
        Set<String> seatNos = new LinkedHashSet<>();
        for (PassengerReq passeger : req.passengers()){
            String seat = normalize(passeger.seatNumber());
            if (!seatNos.add(seat)){
                throw new IllegalArgumentException("Duplicate seat in request" + seat);
            }
        }


        /*Seat Availability & Validity Verification logic!*/
        List<String> layout = busClient.getSeatNumber(trip.busId());
        if (!layout.isEmpty()){
            List<String> invalid = seatNos.stream().filter(seat -> !layout.contains(seat)).toList();
            if (!invalid.isEmpty()){
                throw new IllegalArgumentException("Invalid seats(s) for this bus:" + String.join(",", invalid));
            }
        }


        /* Already locked or booking seats */
        List<TripSeat> taken = tripSeatRepository.findByTripIdAndSeatNumberIn(req.tripId(), seatNos);
        if (!taken.isEmpty()){
            String list = taken.stream().map(TripSeat::getSeatNumber).sorted().collect(Collectors.joining(","));
            throw new ConflictException("Seat(s) not available:" + list);
        }


        String ref = generateRef();
        BigDecimal fare = trip.fare();


        Booking booking = Booking.builder()
                .bookingRef(ref).tripId(trip.tripId() != null ? trip.tripId() : req.tripId())
                .busId(trip.busId()).busName(trip.busName())
                .source(trip.source()).destination(trip.destination())
                .travelDate(trip.travelDate()).destination(trip.destination())
                .userEmail(userEmail)
                .totalAmount(fare.multiply(BigDecimal.valueOf(req.passengers().size())))
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(lockMinutes))
                .build();

        for (PassengerReq p : req.passengers()){
            booking.getPassengers().add(BookingPassenger.builder()
                    .booking(booking).seatNumber(normalize(p.seatNumber()))
                    .passengerName(p.passengerName().trim()).age(p.age())
                    .gender(p.gender().toUpperCase(Locale.ROOT)).fare(fare).build());
        }

        bookingRepository.save(booking);

        List<TripSeat> locks = seatNos.stream()
                .map(s -> TripSeat.builder().tripId(req.tripId()).seatNumber(s)
                        .state(SeatState.LOCKED).bookingRef(ref).build())
                .toList();


        try{
            tripSeatRepository.saveAllAndFlush(locks);
        } catch (RuntimeException e) {
            throw new ConflictException("Seat(s) were just taken by another user. Please choose different seats.");
        }

        log.info("Booking created ref={} trip={} seats={} user={}", ref, req.tripId(), seatNos, userEmail);
        return toRespones(booking);
    }

















    /*Upper to LowerCase*/

    private String normalize(String seat){
        return seat.trim().toLowerCase(Locale.ROOT);
    }


    private String generateRef(){
        return "RB" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }



    private BookingRes toRespones(Booking res){
        List<PassengerRes> passenger = res.getPassengers().stream()
                .map(p -> new PassengerRes(p.getSeatNumber(), p.getPassengerName(),
                        p.getAge(),p.getGender(), p.getFare()))
                .toList();

        return new BookingRes(res.getBookingRef(), res.getStatus(), res.getTripId(), res.getBusName(),
                res.getSource(), res.getDestination(), res.getTravelDate(), res.getDepartureTime(),
                res.getTotalAmount(), res.getExpiresAt(), res.getCreatedAt(), passenger
        );


    }





}

