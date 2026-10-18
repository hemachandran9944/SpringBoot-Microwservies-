package RedBus.Booking.bus.controller;


import RedBus.Booking.bus.dto.*;
import RedBus.Booking.bus.model.*;
import RedBus.Booking.bus.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;

    @PostMapping("/operators")
    public ResponseEntity<ApiResponse<Operator>> createOperator (@Valid @RequestBody OperatorReq req){
        Operator created = busService.createOperator(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Operator creat successfulley!", created));
    }


    @GetMapping("/operators")
    public ResponseEntity<ApiResponse<List<Operator>>> operators(){
        List<Operator> operator = busService.listOperators();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Operators fetched successfully!!", operator));

    }



    @PostMapping("/amenities")
    public ResponseEntity<ApiResponse<Amenity>> createAmenity(@Valid @RequestBody AmenityReq req){
        Amenity creat = busService.createAmenity(req);
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Amenity created successfully!", creat));
    }


    @GetMapping("/amenity")
    public ResponseEntity<ApiResponse<List<Amenity>>> amenity(){
        List<Amenity> amenities = busService.listAmenity();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("amenities fetched successfully!!", amenities));
    }



    @PostMapping
    public ResponseEntity<ApiResponse<BusRes>> addBus(
            @Valid @RequestBody BusReq req,
            @RequestHeader(value = "X-User-Email", required = false) String gmail) throws Exception{
        BusRes busCreate = busService.creatBus(req, gmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bus creat successfulley!", busCreate));

    }


    @GetMapping
    public  ResponseEntity<ApiResponse<Page<BusRes>>> list(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) AcType acType,
            @RequestParam(required = false) BusType busType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    )throws  Exception{
        Page<BusRes> buses = busService.listBus(operatorId, busType, acType,
                PageRequest.of(page, Math.min(size, 50), Sort.by("busName")));
        return ResponseEntity.ok(ApiResponse.success("Buses fetched successfully", buses));
    }




    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusRes>> get(@PathVariable Long id) throws Exception{
        BusRes busGetById = busService.getBus(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Bus fecth by id successfuelly!", busGetById));

    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BusRes>> status(@PathVariable Long id, @RequestParam BusStatus value){
        BusRes status = busService.updateStatus(id, value);
        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Update bus status!", status));

    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> retire (@PathVariable Long id){
        busService.retired(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Bus retired successfully", id));
    }


    @PostMapping("/{id}/seats")
    public ResponseEntity<ApiResponse> seats(@PathVariable  Long id, @Valid @RequestBody List<SeatReq> seats)throws  Exception{
        var seatCount = busService.creatSeatLayout(id, seats);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats booking succcessfully!", seatCount));
    }


    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<SeatReq>>> getSeats(@PathVariable Long id) {
        List<SeatReq> seats = busService.listSeatLayout(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats booking succcessfully!", seats));
    }





}



