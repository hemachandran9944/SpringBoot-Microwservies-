package RedBus.Booking.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
        System.out.println("Booking server runing successfulley!!");
	}
}
