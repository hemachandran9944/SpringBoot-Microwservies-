package RedBus.Booking.bus.exception;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> Conflict(NotFoundException exception){
        return body(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(NotFoundExceptions.class)
    public ResponseEntity<Map<String, Object>>notFound(RuntimeException exception){
        return body(HttpStatus.NOT_FOUND, exception.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> InternalServerError(RuntimeException exception){
        return body(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String msg){
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "error");
        res.put("message", msg);
        return ResponseEntity.status(status).body(res);
    }


}
