package RedBus.Booking.booking.client;

import RedBus.Booking.booking.dto.ApiEnvelope;
import RedBus.Booking.booking.dto.SeatInfo;
import RedBus.Booking.booking.dto.TripInfo;
import RedBus.Booking.booking.exception.NotFoundException;
import RedBus.Booking.booking.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class BusClient{

    private static final ParameterizedTypeReference<ApiEnvelope<TripInfo>> TRIP_TYPE =
            new ParameterizedTypeReference<ApiEnvelope<TripInfo>>() {};

    private static final ParameterizedTypeReference<ApiEnvelope<List<SeatInfo>>> SEATS_TYPE =
            new ParameterizedTypeReference<ApiEnvelope<List<SeatInfo>>>() {};



    private final RestClient restClient; // API Calling Interface Main Keyword "RestClient";


    public BusClient(@Value("${bus.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        /*SimpleClientHttpRequestFactory()-----> This class Config use 'Like Network HttpURLConnection ,Time set method'*/
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }


    // TripInfo Data is Empty Throw Exception

    public TripInfo getTrip (Long tripId){
        try{
            ApiEnvelope<TripInfo> body = restClient.get()
                    .uri("/api/trip/{id}", tripId)
                    .retrieve()
                    .body(TRIP_TYPE);
            if (body == null || body.data() == null){
                throw new NotFoundException("Trip not found:"  + tripId);
            }
            return body.data();
        } catch (HttpClientErrorException.NotFound error) {
            throw new NotFoundException("Trip not found: " + tripId);
        } catch (HttpServerErrorException | ResourceAccessException error) {
            log.error("bus-service call failed: {}", error.getMessage());
            throw new ServiceUnavailableException("Bus service is not available right now. Try again later");
        }
    }


    public List<String> getSeatNumber(Long busId){
        try{
            ApiEnvelope<List<SeatInfo>> body = restClient.get()
                    .uri("/api/bus/{id}/seats", busId)
                    .retrieve()
                    .body(SEATS_TYPE);
            if (body == null || body.data() == null) {
                return List.of();
            }
            return body.data().stream()
                    .map(SeatInfo ::seatNumber)
                    .map(s-> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
        } catch (RestClientException error) {
            log.warn("Seat layout not available for bus {}: {}", busId, error.getMessage());
            return List.of();
        }


    }





}
