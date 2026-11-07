package OnlineBusBooking.Payment.payment_service.repository;

import OnlineBusBooking.Payment.payment_service.model.Payment;
import OnlineBusBooking.Payment.payment_service.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    Optional<Payment> findByPaymentRef(String paymentRef);

    boolean existsByBookingRefAndStatus(String bookingRef, PaymentStatus status);

    Optional<Payment> findFirstByBookingRefAndStatusOrderByCreatedAtDesc(String bookingRef, PaymentStatus status);

    List<Payment> findByBookingRefOrderByCreatedAtDesc(String bookingRef);

    List<Payment> findByUserEmailOrderByCreatedAtDesc(String userEmail);

}
