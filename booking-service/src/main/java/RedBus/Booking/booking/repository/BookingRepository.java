package RedBus.Booking.booking.repository;

import RedBus.Booking.booking.model.Booking;
import RedBus.Booking.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);

}
