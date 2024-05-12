package com.example.demo.controller;

import com.example.demo.dto.trip.TripCreationDTO;
import com.example.demo.dto.trip.TripPatchDTO;
import com.example.demo.entity.Trip;
import com.example.demo.entity.TripDate;
import com.example.demo.entity.User;
import com.example.demo.repository.TripDateRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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
                    .map(User::getGoogleId)
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
}
