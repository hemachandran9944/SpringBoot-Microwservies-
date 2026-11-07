package OnlineBusBooking.Payment.payment_service.controller;


import OnlineBusBooking.Payment.payment_service.dto.ApiResponse;
import OnlineBusBooking.Payment.payment_service.dto.InitiateReq;
import OnlineBusBooking.Payment.payment_service.dto.PayReq;
import OnlineBusBooking.Payment.payment_service.dto.PaymentRes;
import OnlineBusBooking.Payment.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentRes>> initiate(
            @Valid @RequestBody InitiateReq req,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentRes res = paymentService.initiate(req, email, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated. Call /pay to complete.", res));
    }


    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentRes>>> my(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(ApiResponse.success("Payments fetched", paymentService.myPayments(email)));
    }



    @PostMapping("/{paymentRef}/pay")
    public ResponseEntity<ApiResponse<PaymentRes>> pay(
            @PathVariable String paymentRef,
            @Valid @RequestBody PayReq req,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentRes res = paymentService.pay(paymentRef, req, email, role);

        return switch (res.status()) {
            case SUCCESS -> ResponseEntity.ok(ApiResponse.success("Payment successful. Booking confirmed.", res));
            case FAILED -> ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new ApiResponse<>("error", "Payment failed: " + res.failureReason(), res));
            case REFUNDED -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("error", "Payment refunded: " + res.failureReason(), res));
            default -> ResponseEntity.ok(ApiResponse.success("Payment status: " + res.status(), res));
        };
    }


    @GetMapping("/booking/{bookingRef}")
    public ResponseEntity<ApiResponse<List<PaymentRes>>> forBooking(
            @PathVariable String bookingRef,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(ApiResponse.success("Payments fetched",
                paymentService.forBooking(bookingRef, email, role)));
    }



    @PostMapping("/booking/{bookingRef}/refund")
    public ResponseEntity<ApiResponse<PaymentRes>> refund(
            @PathVariable String bookingRef,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentRes res = paymentService.refundForBooking(bookingRef, email, role);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled. Refund amount: " + res.refundAmount(), res));
    }


    @GetMapping("/{paymentRef}")
    public ResponseEntity<ApiResponse<PaymentRes>> get(
            @PathVariable String paymentRef,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched", paymentService.get(paymentRef, email, role)));
    }








}
