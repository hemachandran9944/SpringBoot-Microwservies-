

package OnlineBusBooking.Payment.payment_service.client;

import OnlineBusBooking.Payment.payment_service.dto.ApiEnvelope;
import OnlineBusBooking.Payment.payment_service.dto.BookingInfo;
import OnlineBusBooking.Payment.payment_service.exception.ConflictException;
import OnlineBusBooking.Payment.payment_service.exception.ForbiddenException;
import OnlineBusBooking.Payment.payment_service.exception.NotFoundException;
import OnlineBusBooking.Payment.payment_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;




@Slf4j
@Component
public class BookingClient {
    private static final ParameterizedTypeReference<ApiEnvelope<BookingInfo>> BOOKING_TYPE =
            new ParameterizedTypeReference<ApiEnvelope<BookingInfo>>() {};

    private final RestClient restClient;

    public BookingClient(@Value("${booking.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public BookingInfo getBooking(String ref, String email, String role) {
        try {
            ApiEnvelope<BookingInfo> body = restClient.get()
                    .uri("/api/booking/{ref}", ref)
                    .headers(h -> identity(h, email, role))
                    .retrieve()
                    .body(BOOKING_TYPE);
            if (body == null || body.data() == null) {
                throw new NotFoundException("Booking not found: " + ref);
            }
            return body.data();
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (ResourceAccessException e) {
            throw unavailable(e);
        }
    }

    public void confirm(String ref, String email, String role) {
        try {
            restClient.post()
                    .uri("/api/booking/{ref}/confirm", ref)
                    .headers(h -> identity(h, email, role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (ResourceAccessException e) {
            throw unavailable(e);
        }
    }

    public void cancel(String ref, String email, String role) {
        try {
            restClient.post()
                    .uri("/api/booking/{ref}/cancel", ref)
                    .headers(h -> identity(h, email, role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (ResourceAccessException e) {
            throw unavailable(e);
        }
    }

    /* helpers*/

    private void identity(HttpHeaders headers, String email, String role) {
        headers.set("X-User-Email", email);
        if (role != null && !role.isBlank()) {
            headers.set("X-User-Role", role);
        }
    }

    private RuntimeException translate(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String msg = extractMessage(e, status);
        if (status >= 500) {
            log.error("booking-service error {}: {}", status, msg);
            return new ServiceUnavailableException("Booking service error. Try again later.");
        }
        return switch (status) {
            case 401, 403 -> new ForbiddenException(msg);
            case 404 -> new NotFoundException(msg);
            case 409 -> new ConflictException(msg);
            default -> new IllegalArgumentException(msg);
        };
    }

    private String extractMessage(RestClientResponseException e, int status) {
        try {
            ApiEnvelope<?> env = e.getResponseBodyAs(ApiEnvelope.class);
            if (env != null && env.message() != null) {
                return env.message();
            }
        } catch (Exception ignored) {
            // body parse dons't, default message
        }
        return "Booking service rejected the request (" + status + ")";
    }

    private ServiceUnavailableException unavailable(ResourceAccessException e) {
        log.error("booking-service not reachable: {}", e.getMessage());
        return new ServiceUnavailableException("Booking service is not available right now. Try again later.");
    }

}


