package RedBus.Booking.bus.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "trips", indexes = @Index(name = "idx_trip_search", columnList = "source,destination,travel_date"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id",nullable = false)
    private Bus bus;

    @Column(nullable = false)
    private String source;


    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDate travelDate;


    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(nullable = false)
    private int availableSeats;

}
