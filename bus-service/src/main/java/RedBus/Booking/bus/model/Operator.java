package RedBus.Booking.bus.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    private String contactEmail;
    private String contactPhone;

}
