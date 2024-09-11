package com.example.demo.controller.trip;

import com.example.demo.dto.planner.*;
import com.example.demo.dto.users.user.UserIdsDTO;
import com.example.demo.entity.planner.Accommodation;
import com.example.demo.entity.planner.Location;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.entity.users.user.User;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.tripPlanner.TripDateNotFoundException;
import com.example.demo.service.tripPlanner.TripDatePlannerService;
import com.example.demo.service.tripPlanner.TripNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/planner")
@RequiredArgsConstructor
@Slf4j
public class TripDatePlannerController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripDateRepository tripDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;
    private final JwtCheckService jwtCheckService;
    private final TripDatePlannerService tripDatePlannerService;

    /*
    특정 여행 계획 내 세부 일정 조회
    */
    @GetMapping("/trip/{tripId}/detail")
    public ResponseEntity<List<TripDate>> getTripDetails(@PathVariable("tripId") Long tripId,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            List<TripDate> tripDates = tripDatePlannerService.getTripDates(tripId);
            return new ResponseEntity<>(tripDates, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 작성
     */
    @PostMapping("/trip/{tripId}/detail")
    @Transactional
    public ResponseEntity<TripDateResponseDTO> addTripDetail(@PathVariable("tripId") Long tripId,
                                                             @RequestBody TripDateCreationDTO tripDateCreationDTO,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate createdTripDate = tripDatePlannerService.addTripDetail(tripId, tripDateCreationDTO);
            TripDateResponseDTO responseDTO = new TripDateResponseDTO();
            responseDTO.setTripDateId(createdTripDate.getTripDateId());
            responseDTO.setTripDate(createdTripDate.getTripDate());
            responseDTO.setTripDay(createdTripDate.getTripDay());
            responseDTO.setBudget(createdTripDate.getBudget());
            responseDTO.setAccommodation(new AccommodationResponseDTO(
                    createdTripDate.getAccommodation().getAccommodationName(),
                    createdTripDate.getAccommodation().getAccommodationLocation()
            ));
            responseDTO.setLocations(createdTripDate.getLocations().stream()
                    .map(loc -> new LocationResponseDTO(loc.getLocationName(), loc.getLocationAddress()))
                    .collect(Collectors.toList()));
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 조회
     */
    @GetMapping("/trip/{tripId}/detail/{tripDateId}")
//    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId,
//                                                    @PathVariable("tripDateId") Long tripDateId,
//                                                    HttpServletRequest request,
//                                                    HttpServletResponse response) {
//
//        jwtCheckService.checkJwt(request, response);
//
//        Optional<Trip> tripOptional = tripRepository.findById(tripId);
//        if (!tripOptional.isPresent()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        Optional<TripDate> tripDateOptional = tripDateRepository.findById(tripDateId);
//        if (tripDateOptional.isPresent()) {
//            return new ResponseEntity<>(tripDateOptional.get(), HttpStatus.OK);
//        }
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//
//    }
    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId,
                                                    @PathVariable("tripDateId") Long tripDateId,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate tripDate = tripDatePlannerService.getTripDateById(tripId, tripDateId);
            return new ResponseEntity<>(tripDate, HttpStatus.OK);
        } catch (TripNotFoundException | TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 전체 수정
     */
    @PutMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateCompleteTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                                     @PathVariable("tripDateId") Long tripDateId,
                                                                     @RequestBody TripDatePatchDTO tripDatePatchDTO,
                                                                     HttpServletRequest request,
                                                                     HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateCompleteTripDateDetailById(tripDateId, tripDatePatchDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 일부만 수정
     */
    @PatchMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                             @PathVariable("tripDateId") Long tripDateId,
                                                             @RequestBody TripDatePatchDTO tripDatePatchDTO,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateTripDateDetailById(tripDateId, tripDatePatchDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 삭제
     */
    @DeleteMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<String> deleteTripDateById(@PathVariable("tripId") Long tripId,
                                                     @PathVariable("tripDateId") Long tripDateId,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Optional<TripDate> tripDateOptional = tripDateRepository.findById(tripDateId);

            if (tripDateOptional.isPresent()) {
                TripDate tripDate = tripDateOptional.get();

                // 숙소 정보 삭제 (숙소가 있을 경우)
                if (tripDate.getAccommodation() != null) {
                    accommodationRepository.delete(tripDate.getAccommodation());
                }

                // 위치 정보 삭제 (장소 목록이 있을 경우)
                if (tripDate.getLocations() != null && !tripDate.getLocations().isEmpty()) {
                    locationRepository.deleteAll(tripDate.getLocations());
                }

                // 세부 일정 삭제
                tripDateRepository.delete(tripDate);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else{
                return new ResponseEntity<>("Trip Date not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting Trip Date: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    다른 유저와 여행 계획 공유 및 초대
     */
    @PostMapping("/trip/{tripId}/share")
    public ResponseEntity<String> shareTripPlanWithUser(@PathVariable("tripId") Long tripId,
                                                        @RequestBody UserIdsDTO userIdsDTO,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Optional<Trip> tripOptional = tripRepository.findById(tripId);
            if (tripOptional.isPresent()) {
                Trip trip = tripOptional.get();
                List<User> participants = userRepository.findAllByUserIdIn(userIdsDTO.getUserGoogleIds());
                if (participants.isEmpty()) {
                    return new ResponseEntity<>("User Google IDs are invalid", HttpStatus.BAD_REQUEST);
                }

                // 기존 참여자 목록에 새 참여자 추가
                trip.getParticipants().addAll(participants);
                tripRepository.save(trip);

                return new ResponseEntity<>("User addes to the trip successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Trip not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error share trip plan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}