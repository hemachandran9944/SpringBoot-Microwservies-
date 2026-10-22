package RedBus.Booking.booking.repository;

import RedBus.Booking.booking.model.SeatState;
import RedBus.Booking.booking.model.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TripSeatRepository extends JpaRepository<TripSeat, Long> {

    List<TripSeat> findByTripId(Long tripId);

    List<TripSeat> findByTripIdAndSeatNumberIn(Long tripId, Collection<String> seatNumbers);

    @Modifying(flushAutomatically = true)
    @Query("delete from TripSeat s where s.bookingRef = :ref")
    int deleteByBookingRef(@Param("ref") String ref);

    @Modifying(flushAutomatically = true)
    @Query("update TripSeat s set s.state = :state where s.bookingRef = :ref")
    int updateState(@Param("ref") String ref, @Param("state") SeatState state);


}

