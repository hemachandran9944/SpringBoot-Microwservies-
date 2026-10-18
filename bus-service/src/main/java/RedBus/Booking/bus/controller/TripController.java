package RedBus.Booking.bus.controller;


import RedBus.Booking.bus.dto.ApiResponse;
import RedBus.Booking.bus.dto.TripReq;
import RedBus.Booking.bus.dto.TripRes;
import RedBus.Booking.bus.model.AcType;
import RedBus.Booking.bus.model.BusType;
import RedBus.Booking.bus.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;



@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {
    final  private TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripRes>> tripCreat(@Valid @RequestBody TripReq req){
        try {
            TripRes trip = tripService.createTrip(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Trip creat successfully!", trip));
        } catch (Exception e) {
            throw new RuntimeException("Trip creat error:"+e.getMessage());
        }
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TripRes>>> search (
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BusType busType,
            @RequestParam(required = false) AcType acType,
            @RequestParam(required = false) BigDecimal maxFare,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)LocalTime departAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)LocalTime departBefore,
            @RequestParam(defaultValue =  "departure") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        Page<TripRes> trips = tripService.search(from, to, date, busType, acType, maxFare,
                departAfter, departBefore, sortBy, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Trips search successfully!", trips));

    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripRes>> get(@PathVariable Long id){
        TripRes trip = tripService.get(id);
        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Trips fetch successfully!", trip));  

    }





}
