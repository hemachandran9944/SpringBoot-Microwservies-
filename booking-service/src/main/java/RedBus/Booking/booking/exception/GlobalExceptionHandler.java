package RedBus.Booking.booking.exception;

import RedBus.Booking.booking.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound (NotFoundException error){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.getMessage()));
    }


    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(ConflictException error){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(error.getMessage()));
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> forbidden (ForbiddenException error){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(error.getMessage()));
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ApiResponse<Void>> unvailable (IllegalArgumentException error){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(error.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> invalid(MethodArgumentNotValidException error) {
        Map<String, String> errors = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors()
                .forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("error", "Validation failed", errors));
    }



    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> unreadable (MissingServletRequestParameterException error){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Missing parameter: " + error.getParameterName()));
    }
















}
