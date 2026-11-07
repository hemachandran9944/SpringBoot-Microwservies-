package OnlineBusBooking.Payment.payment_service.service;

import OnlineBusBooking.Payment.payment_service.client.BookingClient;
import OnlineBusBooking.Payment.payment_service.dto.BookingInfo;
import OnlineBusBooking.Payment.payment_service.dto.InitiateReq;
import OnlineBusBooking.Payment.payment_service.dto.PayReq;
import OnlineBusBooking.Payment.payment_service.dto.PaymentRes;
import OnlineBusBooking.Payment.payment_service.exception.ConflictException;
import OnlineBusBooking.Payment.payment_service.exception.ForbiddenException;
import OnlineBusBooking.Payment.payment_service.exception.NotFoundException;
import OnlineBusBooking.Payment.payment_service.model.Payment;
import OnlineBusBooking.Payment.payment_service.model.PaymentStatus;
import OnlineBusBooking.Payment.payment_service.provider.GatewayResult;
import OnlineBusBooking.Payment.payment_service.provider.PaymentGateway;
import OnlineBusBooking.Payment.payment_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Locale;

import static java.text.NumberFormat.Field.CURRENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;
    private final PaymentGateway paymentGateway;

    private static final String CURRENCY = "INR";


    @Value("${payment.refund.full-hours:24}")
    private int fullRefundHours;

    @Value("${payment.refund.partial-hours:6}")
    private int partialRefundHours;

    @Value("${payment.refund.partial-percent:50}")
    private int partialRefundPercent;


    @Transactional
    public PaymentRes initiate(InitiateReq req, String email, String role){
        String bookingRef = req.bookingRef().trim();


        BookingInfo booking = bookingClient.getBooking(bookingRef, email, role);
        assertPayable(booking);

        if (paymentRepository.existsByBookingRefAndStatus(bookingRef, PaymentStatus.SUCCESS)){
            throw new ConflictException("This booking is already paid");
        }


        Optional<Payment> open = paymentRepository
                .findFirstByBookingRefAndStatusOrderByCreatedAtDesc(bookingRef, PaymentStatus.INITIATED);

        if (open.isPresent()) {
            return toResponse(open.get());
        }

        Payment payment = Payment.builder()
                .paymentRef(generateRef())
                .bookingRef(bookingRef)
                .userEmail(email)
                .amount(booking.totalAmount().setScale(2, RoundingMode.HALF_UP))
                .currency(CURRENCY)
                .status(PaymentStatus.INITIATED)
                .build();
        paymentRepository.save(payment);
        log.info("Payment initiated ref={} booking={} amount={}", payment.getPaymentRef(), bookingRef, payment.getAmount());
        return toResponse(payment);


    }



    /*Payment confirm Service*/

    @Transactional
    public PaymentRes pay (String paymentRef, PayReq req, String email, String role){
        Payment payment = findOwned(paymentRef, email, role);

        if (payment.getStatus() == PaymentStatus.SUCCESS){
            return toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new IllegalArgumentException("Payment is " + payment.getStatus() + ". Please start a new payment.");
        }


        BookingInfo booking = bookingClient.getBooking(payment.getBookingRef(), email, role);
        assertPayable(booking);
        if (booking.totalAmount().compareTo(payment.getAmount()) != 0) {
            throw new ConflictException("Booking amount changed. Please start a new payment.");
        }

        boolean simulateFailure = Boolean.TRUE.equals(req.simulateFailure());
        GatewayResult charge = paymentGateway.charge(payment, req.method(), simulateFailure);
        payment.setMethod(req.method());

        if (!charge.success()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(charge.message());
            log.info("Payment failed ref={} reason={}", paymentRef, charge.message());
            return toResponse(payment);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayTxnId(charge.txnId());
        payment.setPaidAt(LocalDateTime.now());

        try {
            bookingClient.confirm(payment.getBookingRef(), email, role);
            log.info("Payment success + booking confirmed ref={} booking={}", paymentRef, payment.getBookingRef());
        } catch (RuntimeException ex) {
            log.error("Booking confirm failed after payment ref={}: {}", paymentRef, ex.getMessage());
            paymentGateway.refund(payment, payment.getAmount());
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(payment.getAmount());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setFailureReason("Auto-refund: booking could not be confirmed (" + ex.getMessage() + ")");
        }
        return toResponse(payment);

    }



    /*Booking cancal and "Refund amount"*/
    @Transactional
    public PaymentRes refundForBooking(String bookingRef, String email, String role){
        Payment payment = paymentRepository
                .findFirstByBookingRefAndStatusOrderByCreatedAtDesc(bookingRef, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new NotFoundException("No successful payment found for booking: " + bookingRef));
        assertOwner(payment, email, role);

        BookingInfo booking = bookingClient.getBooking(bookingRef, email, role);
        int percent = refundPercent(booking);

        // Cancel seat Remove frist. But refund amount doest'
        bookingClient.cancel(bookingRef, email, role);

        BigDecimal refundAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);


        if (refundAmount.signum() > 0) {
            paymentGateway.refund(payment, refundAmount);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment.setRefundedAt(LocalDateTime.now());

        log.info("Refund done booking={} percent={} amount={}", bookingRef, percent, refundAmount);
        return toResponse(payment);

    }


    /*Read method*/

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PaymentRes get(String paymentRef, String email, String role) {
        return toResponse(findOwned(paymentRef, email, role));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PaymentRes> myPayments(String email) {
        return paymentRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream().map(this::toResponse).toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PaymentRes> forBooking(String bookingRef, String email, String role) {
        boolean admin = isAdmin(role);
        return paymentRepository.findByBookingRefOrderByCreatedAtDesc(bookingRef).stream()
                .filter(p -> admin || p.getUserEmail().equalsIgnoreCase(email))
                .map(this::toResponse)
                .toList();
    }













    /*Helper*/


    private Payment findOwned(String paymentRef, String email, String role) {
        Payment payment = paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentRef));
        assertOwner(payment, email, role);
        return payment;
    }


    private void assertOwner(Payment payment, String email, String role) {
        if (!isAdmin(role) && !payment.getUserEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("This payment does not belong to you");
        }
    }



    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }




    private void assertPayable(BookingInfo booking) {

        if (!"PENDING".equalsIgnoreCase(booking.status())) {
            throw new ConflictException("Booking is " + booking.status() + ", payment not allowed");
        }

        if (booking.expiresAt() != null && booking.expiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Seat lock expired. Please book again.");
        }

    }




    private int refundPercent(BookingInfo booking) {
        if (booking.travelDate() == null || booking.departureTime() == null) {
            return 100;
        }
        LocalDateTime departure = LocalDateTime.of(booking.travelDate(), booking.departureTime());
        double hours = Duration.between(LocalDateTime.now(), departure).toMinutes() / 60.0;
        if (hours >= fullRefundHours) return 100;
        if (hours >= partialRefundHours) return partialRefundPercent;
        return 0;
    }




    private String generateRef() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }



    private PaymentRes toResponse(Payment p){
        return new PaymentRes(p.getPaymentRef(), p.getBookingRef(), p.getAmount(), p.getCurrency(),
                p.getStatus(), p.getMethod(), p.getGatewayTxnId(), p.getFailureReason(),
                p.getRefundAmount(), p.getCreatedAt(), p.getPaidAt(), p.getRefundedAt());
    }




}
