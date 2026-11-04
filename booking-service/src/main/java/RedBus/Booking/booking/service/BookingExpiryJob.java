package RedBus.Booking.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/* Each seat before payment and after exprice seat */

@Component
@RequiredArgsConstructor
public class BookingExpiryJob {

    private final BookingService bookingService;
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {
        bookingService.expireOverdue();
    }


}
