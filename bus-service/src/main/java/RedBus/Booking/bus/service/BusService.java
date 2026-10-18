package RedBus.Booking.bus.service;

import RedBus.Booking.bus.dto.*;
import RedBus.Booking.bus.exception.ConflictException;
import RedBus.Booking.bus.model.*;
import RedBus.Booking.bus.repository.AmenityRepository;
import RedBus.Booking.bus.repository.BusRepository;
import RedBus.Booking.bus.repository.OperatorRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class BusService {

    private final AmenityRepository amenityRepository;
    private final BusRepository busRepository;
    private final OperatorRepository operatorRepository;


    @Transactional  
    public Operator createOperator(OperatorReq req) throws RuntimeException{
        if (operatorRepository.existsByLicenseNumber(req.licenseNumber()))
            throw new ConflictException("License already exists:" + req.licenseNumber());

        Operator createNewLicance =  operatorRepository.save(Operator.builder()
                .name(req.name())
                .licenseNumber(req.licenseNumber())
                .contactEmail(req.contactEmail())
                .contactPhone(req.contactPhone())
                .build());
        log.info("License creat successfulley:{}", createNewLicance.getId());
        return  createNewLicance;
    }


    public List<Operator>listOperators(){
        List<Operator> operator = operatorRepository.findAll();
        if (operator.isEmpty()){
            throw  new NotFoundException("Operators record  not found!");
        }
        return  operator;
    }


    @Transactional
    public Amenity createAmenity(AmenityReq req){
        if (amenityRepository.existsByNameIgnoreCase(req.name())){
            throw new ConflictException("AmiAmenity already exists:" + req.name());
        }
        return amenityRepository.save(Amenity.builder().name(req.name()).build());

    }


    public List<Amenity>listAmenity(){
        List<Amenity> amenity = amenityRepository.findAll();
        if (amenity.isEmpty()){
            throw  new NotFoundException("Amenity record not found!");
        }
        return  amenity;
    }



    @Transactional
        public BusRes creatBus(BusReq req, String createdBy){
            if (busRepository.existsByRegistrationNumber(req.registrationNumber())){
                throw new ConflictException("Bus already registered!:" + req.registrationNumber());
            }

        Operator operator = operatorRepository.findById(req.operatorId())
                .orElseThrow(()->new NotFoundException("Operator not found:"+ req.operatorId()));

        Set<Amenity> amenitys  = new HashSet<>();

        if (req.amenityIds() != null && !req.amenityIds().isEmpty()){
            amenitys.addAll(amenityRepository.findAllById(req.amenityIds()));
            if (amenitys.size() != req.amenityIds().size()){
                throw new NotFoundException("Some amenity ids are invalid");
            }
        }

        Bus bus = Bus.builder()
                .busName(req.busName()).registrationNumber(req.registrationNumber())
                .operator(operator).busType(req.busType()).acType(req.acType())
                .totalSeats(req.totalSeats()).amenities(amenitys)
                .createdBy(createdBy).build();
        return toRespones(busRepository.save(bus));
    }


    /* Bus Respones Helpers*/

    BusRes toRespones(Bus res){
        return new BusRes(res.getId(), res.getBusName(), res.getRegistrationNumber(),
                res.getOperator().getName(), res.getOperator().getContactPhone(), res.getOperator().getContactEmail(),
                res.getBusType(), res.getAcType(),
                res.getTotalSeats(), res.getStatus(),
                res.getAmenities().stream().map(Amenity::getName).sorted().toList());
    }

    

    @Transactional(readOnly = true)
    public Page<BusRes> listBus(Long operatorId, BusType busType, AcType acType, Pageable page){
        return busRepository.filter(operatorId, busType, acType, page).map(this ::toRespones);
    }


    @Transactional(readOnly = true)
    public BusRes getBus(Long id){return toRespones(find(id));}






    @Transactional
    public BusRes updateStatus (Long id, BusStatus Status){
        Bus bus = find(id);
        bus.setStatus(Status);
        return toRespones(bus);
    }



    @Transactional
    public void retired(Long id){
        find(id).setStatus(BusStatus.RETIRED);
    }



    @Transactional
    public int creatSeatLayout(Long busId, List<SeatReq> seats){
        Bus bus = find(busId);
        /*Total seat 80 * But user booking 80 seats throw  error--> Only Booking 79 seat throw Exception*/
        if (seats.size() != bus.getTotalSeats()){
            throw new IllegalArgumentException(
                    "Seat count " + seats.size() + " != bus totalSeats " + bus.getTotalSeats());
        }

        Set<String> unique = new HashSet<>();
        // Enhands Condition * Validatione each seats
        for (SeatReq s : seats){
            if (!unique.add(s.seatNumber())){
                throw new IllegalArgumentException("Duplicate seat:" +s.seatNumber());
            }
        }

        /* And clear all seats ** Next build Each Entity Build (return all seat size) */

        bus.getSeats().clear();
        seats.forEach(s -> bus.getSeats().add(SeatLayout.builder()
                .bus(bus).seatNumber(s.seatNumber()).seatRow(s.seatRow())
                .seatColumn(s.seatColumn()).deck(s.deck()).seatType(s.seatType()).build()));
        return seats.size();
    }


    @Transactional(readOnly = true)
    public List<SeatReq> listSeatLayout(Long busId){
        return find(busId).getSeats().stream()
                .map(s -> new SeatReq(s.getSeatNumber(), s.getSeatRow(),
                        s.getSeatColumn(), s.getDeck(), s.getSeatType()))
                .toList();
    }


    /*Bus Id find helper*/
    Bus find(Long id){
        return busRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Bus not found" + id));
    }

}
