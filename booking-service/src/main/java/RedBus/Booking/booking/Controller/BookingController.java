package RedBus.Booking.booking.Controller;

import RedBus.Booking.booking.dto.ApiResponse;
import RedBus.Booking.booking.dto.BookingReq;
import RedBus.Booking.booking.dto.BookingRes;
import RedBus.Booking.booking.dto.SeatStatusRes;
import RedBus.Booking.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingRes>> create(
            @Valid @RequestBody BookingReq req,
            @RequestHeader("X-User-Email") String email){

        BookingRes created = bookingService.createBooking(req, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats locked. Complete payment to confirm.", created));
    }


    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingRes>>> myBookings(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched", bookingService.myBooking(email)));
    }



    @GetMapping("/{ref}")
    public ResponseEntity<ApiResponse<BookingRes>> get(
            @PathVariable String ref,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(ApiResponse.success("Booking fetched", bookingService.get(ref, email, role)));
    }




    @PostMapping("/{ref}/confirm")
    public ResponseEntity<ApiResponse<BookingRes>> confirm(
            @PathVariable String ref,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role){
        return ResponseEntity.ok(ApiResponse.success("Booking Confrimed", bookingService.confrim(ref, email, role)));
    }


    @PostMapping("/{ref}/cancel")
    public ResponseEntity<ApiResponse<BookingRes>> cancel(
            @PathVariable String ref,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role){
        return ResponseEntity.ok(ApiResponse.success("Boooking cancelled", bookingService.cancel(ref, email, role)));
    }



    @GetMapping("/trips/{tripId}/seats")
    public ResponseEntity<ApiResponse<List<SeatStatusRes>>> seats(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success("Seat status fetched", bookingService.seatStatusRes(tripId)));
    }




}
