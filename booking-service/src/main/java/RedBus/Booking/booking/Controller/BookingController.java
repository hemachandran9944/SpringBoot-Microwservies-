package RedBus.Booking.booking.Controller;

import RedBus.Booking.booking.dto.ApiResponse;
import RedBus.Booking.booking.dto.BookingReq;
import RedBus.Booking.booking.dto.BookingRes;
import RedBus.Booking.booking.model.Booking;
import RedBus.Booking.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("api/booking/")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("my")
    public ResponseEntity<ApiResponse<BookingRes>> create(
            @Valid @RequestBody BookingReq req,
            @RequestHeader("X-User-Email") String email){

        BookingRes created = bookingService.createBooking(req, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats locked. Complete payment to confirm.", created));
    }


}
