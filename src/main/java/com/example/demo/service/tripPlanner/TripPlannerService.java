package com.example.demo.service.tripPlanner;

import com.example.demo.dto.planner.TripCreationDTO;
import com.example.demo.dto.planner.TripPatchDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;
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
    public Optional<Trip> getTripById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    @Transactional
    public Trip updateTrip(Long tripId, TripPatchDTO tripPatchDTO) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        List<User> participants = userRepository.findAllById(tripPatchDTO.getParticipantIds());
        trip.setParticipants(participants);

        if (tripPatchDTO.getTripName() != null) trip.setTripName(tripPatchDTO.getTripName());
        if (tripPatchDTO.getStartDate() != null) trip.setStartDate(tripPatchDTO.getStartDate());
        if (tripPatchDTO.getEndDate() != null) trip.setEndDate(tripPatchDTO.getEndDate());
        if (tripPatchDTO.getTripArea() != null) trip.setTripArea(tripPatchDTO.getTripArea());
        if (tripPatchDTO.getBudget() != null) trip.setBudget(tripPatchDTO.getBudget());

        return tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        tripRepository.deleteById(tripId);
    }
}