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
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));
    }

    @Transactional
    public Trip updateTrip(Long tripId, Trip trip) throws TripNotFoundException {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip updateTrip = tripOptional.get();

            List<User> participants = userRepository.findAllById(trip.getParticipants().stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList()));

            updateTrip.setTripName(trip.getTripName());
            updateTrip.setParticipants(participants);
            updateTrip.setStartDate(trip.getStartDate());
            updateTrip.setEndDate(trip.getEndDate());
            updateTrip.setTripArea(trip.getTripArea());
            updateTrip.setBudget(trip.getBudget());

            return tripRepository.save(updateTrip);
        } else {
            throw new TripNotFoundException("Trip with ID " + tripId + " not found");
        }
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        tripRepository.deleteById(tripId);
    }
}