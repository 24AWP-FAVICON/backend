package com.example.demo.service.tripPlanner;

import com.example.demo.dto.planner.TripDateCreationDTO;
import com.example.demo.entity.planner.Accommodation;
import com.example.demo.entity.planner.Location;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripDatePlannerService {

    private final TripRepository tripRepository;
    private final TripDateRepository tripDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TripDate> getTripDates(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));
        return trip.getTripDates();
    }

    @Transactional
    public TripDate addTripDetail(Long tripId, TripDateCreationDTO tripDateDetailsDTO) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));

        // 새로운 TripDate 생성
        TripDate newTripDate = new TripDate();
        newTripDate.setTrip(trip);
        newTripDate.setTripDate(tripDateDetailsDTO.getTripDate());
        newTripDate.setTripDay(tripDateDetailsDTO.getTripDay());
        newTripDate.setBudget(tripDateDetailsDTO.getBudget());

        // TripDate 저장
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

        return savedTripDate;
    }

}