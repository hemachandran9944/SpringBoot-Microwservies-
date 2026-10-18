package RedBus.Booking.bus.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_layouts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bus_id", "seat_number"})
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    private int seatRow;
    private int seatColumn;
    private String deck;        // LOWER, UPPER
    private String seatType;    // WINDOW, AISLE, SLEEPER

}
