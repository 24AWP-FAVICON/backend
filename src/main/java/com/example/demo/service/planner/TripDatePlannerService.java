package com.example.demo.service.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
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
    public TripDate addTripDetail(Long tripId, TripDateRequestDTO tripDateDetailsDTO) {
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

    @Transactional(readOnly = true)
    public TripDate getTripDateById(Long tripId, Long tripDateId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));

        return tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new TripDateNotFoundException("TripDate with ID " + tripDateId + " not found"));
    }

    @Transactional
    public TripDate updateCompleteTripDateDetailById(Long tripDateId, TripDateRequestDTO tripDateRequestDTO) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new TripDateNotFoundException("TripDate with ID " + tripDateId + " not found"));

        // 일자 정보 업데이트
        tripDate.setTripDate(tripDateRequestDTO.getTripDate());
        tripDate.setTripDay(tripDateRequestDTO.getTripDay());
        tripDate.setBudget(tripDateRequestDTO.getBudget());

        // 숙소 정보 업데이트
        Accommodation accommodation = tripDate.getAccommodation();
        accommodation.setAccommodationName(tripDateRequestDTO.getAccommodation().getAccommodationName());
        accommodation.setAccommodationLocation(tripDateRequestDTO.getAccommodation().getAccommodationLocation());
        accommodationRepository.save(accommodation);

        // 위치 정보 업데이트
        List<Location> existingLocations = tripDate.getLocations();
        List<Location> newLocations = tripDateRequestDTO.getLocations().stream()
                .map(locDTO -> new Location(locDTO.getLocationName(), locDTO.getLocationAddress(), tripDate))
                .collect(Collectors.toList());

        // 기존 Location 업데이트 또는 삭제
        for (Location existingLocation : existingLocations) {
            boolean isPresentInNew = newLocations.stream()
                    .anyMatch(newLoc -> newLoc.getLocationName().equals(existingLocation.getLocationName()));

            if (!isPresentInNew) {
                locationRepository.delete(existingLocation);
            } else {
                Location newLocation = newLocations.stream()
                        .filter(newLoc -> newLoc.getLocationName().equals(existingLocation.getLocationName()))
                        .findFirst()
                        .orElse(null);
                if (newLocation != null) {
                    existingLocation.setLocationAddress(newLocation.getLocationAddress());
                    locationRepository.save(existingLocation);
                }
            }
        }

        // 새로 추가할 Location 저장
        for (Location newLocation : newLocations) {
            boolean isExisting = existingLocations.stream()
                    .anyMatch(existingLoc -> existingLoc.getLocationName().equals(newLocation.getLocationName()));

            if (!isExisting) {
                locationRepository.save(newLocation);
            }
        }

        tripDate.setLocations(locationRepository.findByTripDate_TripDateId(tripDateId));

        return tripDateRepository.save(tripDate);
    }

    @Transactional
    public TripDate updateTripDateDetailById(Long tripDateId, TripDateRequestDTO tripDateRequestDTO) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new TripDateNotFoundException("TripDate with ID " + tripDateId + " not found"));

        // 일자 정보 업데이트 (optional)
        if (tripDateRequestDTO.getTripDate() != null) {
            tripDate.setTripDate(tripDateRequestDTO.getTripDate());
        }
        if (tripDateRequestDTO.getTripDay() != null) {
            tripDate.setTripDay(tripDateRequestDTO.getTripDay());
        }
        if (tripDateRequestDTO.getBudget() != null){
            tripDate.setBudget(tripDateRequestDTO.getBudget());
        }

        // 숙소 정보 업데이트(optional)
        if (tripDateRequestDTO.getAccommodation() != null) {
            Accommodation accommodation = tripDate.getAccommodation();
            accommodation.setAccommodationName(tripDateRequestDTO.getAccommodation().getAccommodationName());
            accommodation.setAccommodationLocation(tripDateRequestDTO.getAccommodation().getAccommodationLocation());
            accommodationRepository.save(accommodation);
        }

        // 위치 정보 업데이트 (옵셔널)
        if (tripDateRequestDTO.getLocations() != null && !tripDateRequestDTO.getLocations().isEmpty()) {
            // 기존 위치 정보 삭제
            locationRepository.deleteAll(tripDate.getLocations());
            List<Location> updatedLocations = tripDateRequestDTO.getLocations().stream()
                    .map(locDTO -> new Location(locDTO.getLocationName(), locDTO.getLocationAddress(), tripDate))
                    .collect(Collectors.toList());
            locationRepository.saveAll(updatedLocations);
            tripDate.setLocations(updatedLocations);
        }

        return tripDateRepository.save(tripDate);
    }

    @Transactional
    public void deleteTripDateById(Long tripDateId) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new TripDateNotFoundException("TripDate with ID " + tripDateId + " not found"));

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
    }

}