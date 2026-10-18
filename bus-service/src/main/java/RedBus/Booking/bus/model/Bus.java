package RedBus.Booking.bus.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false)
    private String busName;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType busType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcType acType;

    @Column(nullable = false)
    private int totalSeats;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BusStatus status = BusStatus.ACTIVE;

    @ManyToMany
    @JoinTable(name = "bus_amenities",
               joinColumns = @JoinColumn(name = "bus_id"),
               inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();


    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeatLayout> seats = new ArrayList<>();

    private String createdBy;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
