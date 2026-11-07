package OnlineBusBooking.Payment.payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_booking", columnList = "booking_ref"),
        @Index(name = "idx_payment_user", columnList = "user_email")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_ref", nullable = false, unique = true, length = 20)
    private String paymentRef;

    // Cross-service reference (booking-service): plain value, DB foreign key illa
    @Column(name = "booking_ref", nullable = false, length = 20)
    private String bookingRef;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String gatewayTxnId;
    private String failureReason;

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    // Optimistic lock:  Two memper pay  amount same time but one pepole win
    @Version
    private Long version;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }






}
