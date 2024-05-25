package com.example.demo.controller;

import com.example.demo.dto.TripCreationDTO;
import com.example.demo.dto.TripDateDetailsDTO;
import com.example.demo.dto.TripPatchDTO;
import com.example.demo.dto.UserIdsDTO;
import com.example.demo.entity.planner.Accommodation;
import com.example.demo.entity.planner.Location;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.entity.User;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
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

    /*
    전체 여행 계획 조회
     */
    @GetMapping("/trip")
    public ResponseEntity<List<Trip>> getAllTrips() {
        try {
            List<Trip> trips = tripRepository.findAll();

            if (trips.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(trips, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    전체 여행 계획 생성
     */
    @PostMapping("/trip")
    public ResponseEntity<?> addNewTrip(@Valid @RequestBody TripCreationDTO tripDTO) {
       try {
           // 1. 참여자 확인
           List<User> participants = userRepository.findAllById(tripDTO.getParticipantIds());
           if (tripDTO.getParticipantIds() == null || tripDTO.getParticipantIds().isEmpty()) {
               return new ResponseEntity<>("Participant IDs must be provided.", HttpStatus.BAD_REQUEST);
           }

           // 2. Trip 객체 생성
           Trip newTrip = new Trip();
           newTrip.setTripName(tripDTO.getTripName());
           newTrip.setTripArea(tripDTO.getTripArea());
           newTrip.setStartDate(tripDTO.getStartDate());
           newTrip.setEndDate(tripDTO.getEndDate());
           newTrip.setBudget(tripDTO.getBudget());
           newTrip.setParticipants(participants);

           // 3. 저장
           Trip savedTrip = tripRepository.save(newTrip);

           return new ResponseEntity<>(savedTrip, HttpStatus.CREATED);
       }
       catch (Exception e){
           e.printStackTrace();
           return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

       }
    }

    /*
    특정 여행 계획 조회
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Trip> getTripById(@PathVariable("tripId") Long tripId) {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            return new ResponseEntity<>(tripOptional.get(), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*
    특정 여행 계획 수정
     */
    @PutMapping("/trip/{tripId}")
    public ResponseEntity<Trip> updateTripById(@PathVariable("tripId") Long tripId, @RequestBody Trip trip) {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip updateTrip = tripOptional.get();
            // 참여자 ID 리스트를 통해 User 객체들을 조회
            List<User> participants = userRepository.findAllById(trip.getParticipants().stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList()));

            // Trip 객체의 모든 필드 업데이트
            updateTrip.setTripName(trip.getTripName());
            updateTrip.setParticipants(participants); // 업데이트된 참여자 목록 설정
            updateTrip.setStartDate(trip.getStartDate());
            updateTrip.setEndDate(trip.getEndDate());
            updateTrip.setTripArea(trip.getTripArea());
            updateTrip.setBudget(trip.getBudget());

            // 데이터베이스에 저장
            tripRepository.save(updateTrip);

            return new ResponseEntity<>(updateTrip, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*
    특정 여행 계획 일부 수정
     */
    @PatchMapping("/trip/{tripId}")
    public ResponseEntity<Trip> updateTripById(@PathVariable("tripId") Long tripId, @RequestBody TripPatchDTO tripPatchDTO) {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip updateTrip = tripOptional.get();
            if (tripPatchDTO.getParticipantIds() != null) {
                List<User> participants = userRepository.findAllById(tripPatchDTO.getParticipantIds());
                updateTrip.setParticipants(participants);
            }
            if (tripPatchDTO.getTripName() != null) updateTrip.setTripName(tripPatchDTO.getTripName());
            if (tripPatchDTO.getStartDate() != null) updateTrip.setStartDate(tripPatchDTO.getStartDate());
            if (tripPatchDTO.getEndDate() != null) updateTrip.setEndDate(tripPatchDTO.getEndDate());
            if (tripPatchDTO.getTripArea() != null) updateTrip.setTripArea(tripPatchDTO.getTripArea());
            if (tripPatchDTO.getBudget() != null) updateTrip.setBudget(tripPatchDTO.getBudget());

            tripRepository.save(updateTrip);
            return new ResponseEntity<>(updateTrip, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    /*
    특정 여행 계획 삭제
     */
    @DeleteMapping("/trip/{tripId}")
    public ResponseEntity<String> deleteTripById(@PathVariable("tripId") Long tripId) {
        try {
            tripRepository.deleteById(tripId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 조회
    */
    @GetMapping("/trip/{tripId}/detail")
    ResponseEntity<List<TripDate>> getTripDetails(@PathVariable("tripId") Long tripId) {
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
    ResponseEntity<?> addTripDetail(@PathVariable("tripId") Long tripId, @RequestBody TripDateDetailsDTO tripDateDetailsDTO) {
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
    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId, @PathVariable("tripDateId") Long tripDateId) {
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
                                                              @RequestBody TripDateDetailsDTO tripDateDetailsDTO) {
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
                                                             @RequestBody TripDateDetailsDTO tripDateDetailsDTO) {
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
    public ResponseEntity<String> deleteTripDateById(@PathVariable("tripId") Long tripId, @PathVariable("tripDateId") Long tripDateId) {
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
    public ResponseEntity<String> shareTripPlanWithUser(@PathVariable("tripId") Long tripId, @RequestBody UserIdsDTO userIdsDTO) {
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