package RedBus.Booking.booking.Controller;

import RedBus.Booking.booking.dto.ApiResponse;
import RedBus.Booking.booking.dto.BookingReq;
import RedBus.Booking.booking.dto.BookingRes;
import RedBus.Booking.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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



    // Getting Booking Booking API
    
    @PostMapping("/{ref}/confirm")
    public ResponseEntity<ApiResponse<BookingRes>> get(
            @PathVariable String ref,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role){
        return ResponseEntity.ok(ApiResponse.success("Booking Confrimed", bookingService.confrim(ref, email, role)));
    }





}
