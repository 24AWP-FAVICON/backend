package com.example.demo.service.tripPlanner;

import com.example.demo.dto.planner.TripCreationDTO;
import com.example.demo.dto.planner.TripPatchDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.InvalidUserException;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlannerService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Transactional
    public Trip createTrip(TripCreationDTO tripDTO) {
        // 1. 참여자 확인
        List<User> participants = userRepository.findAllById(tripDTO.getParticipantIds());

        // 2. Trip 객체 생성
        Trip newTrip = new Trip();
        newTrip.setTripName(tripDTO.getTripName());
        newTrip.setTripArea(tripDTO.getTripArea());
        newTrip.setStartDate(tripDTO.getStartDate());
        newTrip.setEndDate(tripDTO.getEndDate());
        newTrip.setBudget(tripDTO.getBudget());
        newTrip.setParticipants(participants);

        // 3. 저장
        return tripRepository.save(newTrip);
    }

    @Transactional(readOnly = true)
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));
    }

    @Transactional
    public Trip updateTrip(Long tripId, Trip trip) throws TripNotFoundException {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip updateTrip = tripOptional.get();

            // 참여자 ID 리스트를 통해 User 객체들을 조회
            List<User> participants = userRepository.findAllById(trip.getParticipants().stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList()));

            // Trip 객체의 모든 필드 업데이트
            updateTrip.setTripName(trip.getTripName());
            updateTrip.setParticipants(participants);
            updateTrip.setStartDate(trip.getStartDate());
            updateTrip.setEndDate(trip.getEndDate());
            updateTrip.setTripArea(trip.getTripArea());
            updateTrip.setBudget(trip.getBudget());

            // 데이터베이스에 저장
            return tripRepository.save(updateTrip);
        } else {
            throw new TripNotFoundException("Trip with ID " + tripId + " not found");
        }
    }

    @Transactional
    public Trip partialUpdateTrip(Long tripId, TripPatchDTO tripPatchDTO) {
        Trip updateTrip = getTripById(tripId);

        if (tripPatchDTO.getParticipantIds() != null) {
            List<User> participants = userRepository.findAllById(tripPatchDTO.getParticipantIds());
            updateTrip.setParticipants(participants);
        }
        if (tripPatchDTO.getTripName() != null) updateTrip.setTripName(tripPatchDTO.getTripName());
        if (tripPatchDTO.getStartDate() != null) updateTrip.setStartDate(tripPatchDTO.getStartDate());
        if (tripPatchDTO.getEndDate() != null) updateTrip.setEndDate(tripPatchDTO.getEndDate());
        if (tripPatchDTO.getTripArea() != null) updateTrip.setTripArea(tripPatchDTO.getTripArea());
        if (tripPatchDTO.getBudget() != null) updateTrip.setBudget(tripPatchDTO.getBudget());

        return tripRepository.save(updateTrip);
    }

    @Transactional
    public void deleteTripById(Long tripId) {
        tripRepository.deleteById(tripId);
    }

    @Transactional
    public void shareTripPlanWithUser(Long tripId, List<String> userGoogleIds) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));

        List<User> participants = userRepository.findAllByUserIdIn(userGoogleIds);
        if (participants.isEmpty()) {
            throw new InvalidUserException("User Google IDs are invalid");
        }

        // 기존 참여자 목록을 가져온다
        List<User> existingParticipants = trip.getParticipants();

        // 이미 참여자 목록에 있는 사용자를 제외한 새로운 사용자만 추가한다
        List<User> newParticipants = participants.stream()
                .filter(user -> !existingParticipants.contains(user))
                .collect(Collectors.toList());

        if (!newParticipants.isEmpty()) {
            existingParticipants.addAll(newParticipants);
            tripRepository.save(trip);
        }
    }
}