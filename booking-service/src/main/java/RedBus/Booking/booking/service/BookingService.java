package RedBus.Booking.booking.service;


import RedBus.Booking.booking.client.BusClient;
import RedBus.Booking.booking.dto.*;
import RedBus.Booking.booking.exception.ConflictException;
import RedBus.Booking.booking.exception.ForbiddenException;
import RedBus.Booking.booking.exception.NotFoundException;
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
import java.util.function.Function;
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
        expireOverdue();


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
                .travelDate(trip.travelDate()).departureTime(trip.departureTime())
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



    @Transactional
    public BookingRes confrim (String ref, String userEmail, String role)   {
        Booking booking = findOwned(ref, userEmail, role);

        if (booking.getStatus() == BookingStatus.CONFIRMED){
            return toRespones(booking);  // idempotent
        }

        if (booking.getStatus() != BookingStatus.PENDING){
            throw new IllegalArgumentException("Booking is" + booking.getStatus() + ", cannot confrim");
        }

        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Seat lock expired. Please book again.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        tripSeatRepository.updateState(ref, SeatState.BOOKED);

        log.info("Booking confrimed ref={}", ref);
        return toRespones(booking);
    }


    // Cancel Service
    @Transactional
    public BookingRes cancel (String ref, String userEmail, String role){
        Booking booking = findOwned(ref, userEmail, role);

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.EXPIRED){
            throw new IllegalArgumentException("Booking is already" + booking.getStatus());
        }

        LocalDateTime departure =  LocalDateTime.of(booking.getTravelDate(), booking.getDepartureTime());

        if (departure.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Trip already departed,  cannot cancel");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setExpiresAt(null);
        tripSeatRepository.deleteByBookingRef(ref);

        log.info("Booking cancelled ref={} by={}", ref, userEmail);
        return toRespones(booking);
    }


    /*Read Service*/
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public BookingRes get(String ref, String userEmail, String role){
        return toRespones(findOwned(ref, userEmail, role));
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<BookingRes> myBooking(String userEmail){
        return bookingRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream().map(this::toRespones).toList();
    }




    @Transactional
    public List<SeatStatusRes> seatStatusRes (Long tripid){
        expireOverdue();
        
        TripInfo trip = busClient.getTrip(tripid);

        Map<String, TripSeat> taken = tripSeatRepository.findByTripId(tripid).stream()
                .collect(Collectors.toMap(TripSeat :: getSeatNumber, Function.identity()));
        List<String> layout = busClient.getSeatNumber(trip.busId());

        if (layout.isEmpty()){
            return taken.values().stream()
                    .map(s -> new SeatStatusRes(s.getSeatNumber(), s.getState().name()))
                    .sorted(Comparator.comparing(SeatStatusRes::seatNumber))
                    .toList();
        }

        return layout.stream()
                .map(s -> new SeatStatusRes(s, taken.containsKey(s) ? taken.get(s).getState().name() : "AVAILABLE"))
                .toList();

    }





    /*Pending and time expired seat release*/
    @Transactional
    public int expireOverdue() {
        List<Booking> overdue = bookingRepository
                .findByStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

        for (Booking b : overdue){
            b.setStatus(BookingStatus.EXPIRED);
            tripSeatRepository.deleteByBookingRef(b.getBookingRef());
        }
        if (!overdue.isEmpty()){
            log.info("Expired {} pending booking(s)", overdue.stream());
        }
        return overdue.size();
    }




    /* Bus Booking id  find */
    private Booking findOwned (String ref, String userEmail, String role){
        Booking booking = bookingRepository.findByBookingRef(ref)
                .orElseThrow(()-> new NotFoundException("Booking not found:" + ref));

        boolean admin = "ADMIN".equalsIgnoreCase(role);
        if (!admin && !booking.getUserEmail().equalsIgnoreCase(userEmail)){
            throw new ForbiddenException("This booking does not belong to you");
        }
        return booking;
    }







    /*Upper to LowerCase*/
    private String normalize(String seat){
        return seat.trim().toLowerCase(Locale.ROOT);
    }


    // Trancasation  Random Id
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

