package RedBus.Booking.bus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BusServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(BusServiceApplication.class, args);
        System.out.println("Bus Server is running successfuelly!");
	}
}
