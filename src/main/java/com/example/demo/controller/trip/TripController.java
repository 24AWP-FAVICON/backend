package com.example.demo.controller.trip;

import com.example.demo.dto.planner.TripCreationDTO;
import com.example.demo.dto.planner.TripDateDetailsDTO;
import com.example.demo.dto.planner.TripPatchDTO;
import com.example.demo.dto.planner.TripResponseDTO;
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
import com.example.demo.service.tripPlanner.TripNotFoundException;
import com.example.demo.service.tripPlanner.TripPlannerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
public class TripController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripDateRepository tripDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;
    private final JwtCheckService jwtCheckService;
    private final TripPlannerService tripPlannerService;

    /*
    전체 여행 계획 조회
     */
    @GetMapping("/trip")
    public ResponseEntity<List<TripResponseDTO>> getAllTrips(HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);
        try {
            List<Trip> trips = tripPlannerService.getAllTrips();
            List<TripResponseDTO> responseDTOs = trips.stream()
                    .map(TripResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            if (trips.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    전체 여행 계획 생성
     */
    @PostMapping("/trip")
    public ResponseEntity<Trip> addNewTrip(@RequestBody TripCreationDTO tripDTO,
                                           HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            Trip createdTrip = tripPlannerService.createTrip(tripDTO);
            return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 조회
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable("tripId") Long tripId,
                                            HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            Trip trip = tripPlannerService.getTripById(tripId);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(trip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 수정
     */
    @PutMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> updateTripById(@PathVariable("tripId") Long tripId,
                                               @RequestBody Trip trip,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Trip updatedTrip = tripPlannerService.updateTrip(tripId, trip);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(updatedTrip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 일부 수정
     */
    @PatchMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> partialUpdateTripById(@PathVariable("tripId") Long tripId,
                                                      @RequestBody TripPatchDTO tripPatchDTO,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Trip updatedTrip = tripPlannerService.partialUpdateTrip(tripId, tripPatchDTO);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(updatedTrip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 삭제
     */
    @DeleteMapping("/trip/{tripId}")
    public ResponseEntity<String> deleteTripById(@PathVariable("tripId") Long tripId,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            tripPlannerService.deleteTripById(tripId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 조회
    */
    @GetMapping("/trip/{tripId}/detail")
    ResponseEntity<List<TripDate>> getTripDetails(@PathVariable("tripId") Long tripId,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);
        Optional<Trip> tripDetails = tripRepository.findById(tripId);
        if (!tripDetails.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<TripDate> tripDates = tripDetails.get().getTripDates();
        return new ResponseEntity<>(tripDates, HttpStatus.OK);
    }

    /*
    특정 여행 계획 내 세부 일정 작성
     */
    @PostMapping("/trip/{tripId}/detail")
    @Transactional
    ResponseEntity<?> addTripDetail(@PathVariable("tripId") Long tripId,
                                    @RequestBody TripDateDetailsDTO tripDateDetailsDTO,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Optional<Trip> tripOptional = tripRepository.findById(tripId);
            if (tripOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            TripDate newTripDate = new TripDate();
            newTripDate.setTrip(tripOptional.get());
            newTripDate.setTripDate(tripDateDetailsDTO.getTripDate());
            newTripDate.setTripDay(tripDateDetailsDTO.getTripDay());
            newTripDate.setBudget(tripDateDetailsDTO.getBudget());

            // 먼저 위 내용 저장
            TripDate savedTripDate = tripDateRepository.save(newTripDate);

            // 숙소 처리
            Accommodation accommodation = new Accommodation();
            accommodation.setAccommodationName(tripDateDetailsDTO.getAccommodation().getAccommodationName());
            accommodation.setAccommodationLocation(tripDateDetailsDTO.getAccommodation().getAccommodationLocation());
            accommodation.setTripDate(savedTripDate);
            accommodationRepository.save(accommodation);
            savedTripDate.setAccommodation(accommodation);

            // 장소 처리
            List<Location> locations = tripDateDetailsDTO.getLocations().stream()
                    .map(locDTO -> {
                        Location location = new Location();
                        location.setLocationName(locDTO.getLocationName());
                        location.setLocationAddress(locDTO.getLocationAddress());
                        location.setTripDate(savedTripDate);
                        return location;
                    })
                    .collect(Collectors.toList());
            locationRepository.saveAll(locations);
            savedTripDate.setLocations(locations);

            return new ResponseEntity<>(savedTripDate, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 조회
     */
    @GetMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId,
                                                    @PathVariable("tripDateId") Long tripDateId,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        Optional<Trip> tripOptional = tripRepository.findById(tripId);
        if (!tripOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<TripDate> tripDateOptional = tripDateRepository.findById(tripDateId);
        if (tripDateOptional.isPresent()) {
            return new ResponseEntity<>(tripDateOptional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    /*
    특정 여행 계획 내 세부 일정 전체 수정
     */
    @PutMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateCompleteTripDateDetailById( @PathVariable("tripId") Long tripId,
                                                                      @PathVariable("tripDateId") Long tripDateId,
                                                                      @RequestBody TripDateDetailsDTO tripDateDetailsDTO,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        Optional<TripDate> tripDateOptional = tripDateRepository.findById(tripDateId);

        if (tripDateOptional.isPresent()) {
            TripDate tripDate = tripDateOptional.get();

            // 일자 정보 업데이트
            tripDate.setTripDate(tripDateDetailsDTO.getTripDate());
            tripDate.setTripDay(tripDateDetailsDTO.getTripDay());
            tripDate.setBudget(tripDateDetailsDTO.getBudget());

            // 숙소 정보 업데이트
            Accommodation accommodation = tripDate.getAccommodation();
            accommodation.setAccommodationName(tripDateDetailsDTO.getAccommodation().getAccommodationName());
            accommodation.setAccommodationLocation(tripDateDetailsDTO.getAccommodation().getAccommodationLocation());
            accommodationRepository.save(accommodation);

            // 위치 정보 업데이트
            tripDate.getLocations().clear(); // 기존 위치 정보는 삭제
            List<Location> newLocations = tripDateDetailsDTO.getLocations().stream()
                    .map(locDTO -> new Location(locDTO.getLocationName(), locDTO.getLocationAddress(), tripDate))
                    .collect(Collectors.toList());
            locationRepository.saveAll(newLocations);
            tripDate.setLocations(newLocations);

            tripDateRepository.save(tripDate);
            return new ResponseEntity<>(tripDate, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 일부만 수정
     */
    @PatchMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                             @PathVariable("tripDateId") Long tripDateId,
                                                             @RequestBody TripDateDetailsDTO tripDateDetailsDTO,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);
        Optional<TripDate> tripDateOptional = tripDateRepository.findById(tripDateId);

        if (tripDateOptional.isPresent()) {
            TripDate tripDate = tripDateOptional.get();

            // 일자 정보 업데이트 (optional)
            if (tripDateDetailsDTO.getTripDate() != null) {
                tripDate.setTripDate(tripDateDetailsDTO.getTripDate());
            }
            if (tripDateDetailsDTO.getTripDay() != null) {
                tripDate.setTripDay(tripDateDetailsDTO.getTripDay());
            }
            if (tripDateDetailsDTO.getBudget() != null){
                tripDate.setBudget(tripDateDetailsDTO.getBudget());
            }

            // 숙소 정보 업데이트(optional)
            if (tripDateDetailsDTO.getAccommodation() != null) {
                Accommodation accommodation = tripDate.getAccommodation();
                accommodation.setAccommodationName(tripDateDetailsDTO.getAccommodation().getAccommodationName());
                accommodation.setAccommodationLocation(tripDateDetailsDTO.getAccommodation().getAccommodationLocation());
                accommodationRepository.save(accommodation);
            }

            // 위치 정보 업데이트 (옵셔널)
            if (tripDateDetailsDTO.getLocations() != null && !tripDateDetailsDTO.getLocations().isEmpty()) {
                // 기존 위치 정보 삭제
                locationRepository.deleteAll(tripDate.getLocations());
                List<Location> updatedLocations = tripDateDetailsDTO.getLocations().stream()
                        .map(locDTO -> new Location(locDTO.getLocationName(), locDTO.getLocationAddress(), tripDate))
                        .collect(Collectors.toList());
                locationRepository.saveAll(updatedLocations);
                tripDate.setLocations(updatedLocations);
            }

            TripDate updatedTripDate = tripDateRepository.save(tripDate);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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