package RedBus.Booking.booking.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "trip_seats",
        uniqueConstraints = @UniqueConstraint(name = "uk_trip_seat", columnNames = {"trip_id", "seat_number"}),
        indexes = @Index(name = "idx_trip_seat_ref", columnList = "booking_ref"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatState state;

    @Column(name = "booking_ref", nullable = false, length = 20)
    private String bookingRef;


}
