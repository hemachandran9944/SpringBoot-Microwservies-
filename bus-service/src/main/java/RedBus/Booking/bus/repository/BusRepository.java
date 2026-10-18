package RedBus.Booking.bus.repository;

import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.Bus;
import RedBus.Booking.bus.model.BusType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

public interface BusRepository  extends JpaRepository<Bus,Long> {
    boolean existsByRegistrationNumber(String registrationNumber);


    @Query("""
        select b from Bus b
        where (:operatorId is null or b.operator.id = :operatorId)
          and (:busType is null or b.busType = :busType)
          and (:acType is null or b.acType = :acType)
          and b.status <> RedBus.Booking.bus.model.BusStatus.RETIRED
        """)
    
    Page<Bus> filter(@Param("operatorId") Long operator_id,
                          @Param("busType") BusType busType,
                          @Param("acType") AcType acType,
                          Pageable pageable);



}

