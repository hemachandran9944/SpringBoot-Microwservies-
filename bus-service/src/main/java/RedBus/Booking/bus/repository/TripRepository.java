package RedBus.Booking.bus.repository;

import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.BusType;
import RedBus.Booking.bus.model.Trip;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface TripRepository extends JpaRepository <Trip, Long> {
    @Query("""
        select t from Trip t join t.bus b
        where lower(t.source) = lower(:source)
          and lower(t.destination) = lower(:destination)
          and t.travelDate = :date
          and t.availableSeats > 0
          and b.status = RedBus.Booking.bus.model.BusStatus.ACTIVE
          and (:busType is null or b.busType = :busType)
          and (:acType is null or b.acType = :acType)
          and (:maxFare is null or t.fare <= :maxFare)
          and (:departAfter is null or t.departureTime >= :departAfter)
          and (:departBefore is null or t.departureTime <= :departBefore)
        """)
    Page<Trip> search(@Param("source") String source,
                      @Param("destination") String destination,
                      @Param("date") LocalDate date,
                      @Param("busType") BusType busType,
                      @Param("acType") AcType acType,
                      @Param("maxFare") BigDecimal maxFare,
                      @Param("departAfter") LocalTime departAfter,
                      @Param("departBefore") LocalTime departBefore,
                      Pageable pageable);

}
