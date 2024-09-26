package com.example.demo.service.planner;

import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.entity.planner.Accommodation;
import com.example.demo.entity.planner.Location;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 이 서비스 클래스는 여행 계획의 세부 일정과 관련된 비즈니스 로직을 처리합니다.
 * 일정의 조회, 추가, 수정, 삭제 기능을 제공합니다.
 * @author minjeong
 * @see TripRepository 여행 계획을 저장하고 조회하는 리포지토리
 * @see TripDateRepository 여행 계획 내의 세부 일정을 저장하고 조회하는 리포지토리
 * @see AccommodationRepository 숙소 정보를 저장하고 조회하는 리포지토리
 * @see LocationRepository 위치(장소) 정보를 저장하고 조회하는 리포지토리
 * @see TripDateRequestDTO 세부 일정 요청 데이터를 담고 있는 DTO
 * @see TripDate 여행 계획 내의 세부 일정을 나타내는 엔티티 클래스
 * @see Accommodation 여행 계획 내의 숙소 정보를 나타내는 엔티티 클래스
 * @see Location 여행 계획 내의 위치(장소) 정보를 나타내는 엔티티 클래스
 */
@Service
@RequiredArgsConstructor
public class TripDatePlannerService {

    private final TripRepository tripRepository;
    private final TripDateRepository tripDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;

    /**
     * 특정 여행 계획의 모든 세부 일정을 조회합니다.
     * 주어진 여행 ID에 해당하는 모든 일정을 가져옵니다.
     *
     * @param tripId 조회할 여행 계획의 ID
     * @return 해당 여행 계획에 속한 모든 세부 일정 리스트
     * @throws ComponentNotFoundException 여행 계획을 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public List<TripDate> getTripDates(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ComponentNotFoundException("Trip with ID " + tripId + " not found"));
        return trip.getTripDates();
    }

    /**
     * 새로운 세부 일정을 추가합니다.
     * 주어진 여행 계획 ID와 세부 일정 정보를 사용하여 새로운 일정을 생성합니다.
     *
     * @param tripId             추가할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateDetailsDTO 추가할 세부 일정 정보가 담긴 DTO
     * @return 생성된 세부 일정 객체
     * @throws ComponentNotFoundException 여행 계획을 찾을 수 없는 경우 발생
     */
    @Transactional
    public TripDate addTripDetail(Long tripId, TripDateRequestDTO tripDateDetailsDTO) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ComponentNotFoundException("Trip with ID " + tripId + " not found"));

        // 새로운 TripDate 생성
        TripDate newTripDate = new TripDate();
        newTripDate.setTrip(trip);
        newTripDate.setTripDate(tripDateDetailsDTO.getTripDate());
        newTripDate.setTripDay(tripDateDetailsDTO.getTripDay());
        newTripDate.setBudget(tripDateDetailsDTO.getBudget());

        // TripDate 저장
        TripDate savedTripDate = tripDateRepository.save(newTripDate);

        // 숙소 정보가 있는 경우에만 처리
        if (tripDateDetailsDTO.getAccommodation() != null) {
            Accommodation accommodation = new Accommodation();
            accommodation.setAccommodationName(tripDateDetailsDTO.getAccommodation().getAccommodationName());
            accommodation.setAccommodationLocation(tripDateDetailsDTO.getAccommodation().getAccommodationLocation());
            accommodation.setTripDate(savedTripDate);
            accommodationRepository.save(accommodation);
            savedTripDate.setAccommodation(accommodation);
        }

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

    /**
     * 특정 세부 일정을 조회합니다.
     * 주어진 여행 계획 ID와 세부 일정 ID를 사용하여 일정을 조회합니다.
     *
     * @param tripId     조회할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateId 조회할 세부 일정의 ID
     * @return 조회된 세부 일정 객체
     * @throws ComponentNotFoundException    여행 계획 또는 세부 일정을 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public TripDate getTripDateById(Long tripId, Long tripDateId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ComponentNotFoundException("Trip with ID " + tripId + " not found"));

        return tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new ComponentNotFoundException("TripDate with ID " + tripDateId + " not found"));
    }

    /**
     * 특정 세부 일정의 모든 정보를 수정합니다.
     * 주어진 세부 일정 ID와 수정할 정보를 사용하여 일정을 업데이트합니다.
     *
     * @param tripDateId         수정할 세부 일정의 ID
     * @param tripDateRequestDTO 수정할 정보가 담긴 DTO
     * @return 수정된 세부 일정 객체
     * @throws ComponentNotFoundException 세부 일정을 찾을 수 없는 경우 발생
     */
    @Transactional
    public TripDate updateCompleteTripDateDetailById(Long tripDateId, TripDateRequestDTO tripDateRequestDTO) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new ComponentNotFoundException("TripDate with ID " + tripDateId + " not found"));

        // 일자 정보 업데이트
        tripDate.setTripDate(tripDateRequestDTO.getTripDate());
        tripDate.setTripDay(tripDateRequestDTO.getTripDay());
        tripDate.setBudget(tripDateRequestDTO.getBudget());

        // 숙소 정보가 존재할 때만 업데이트
        if (tripDateRequestDTO.getAccommodation() != null) {
            Accommodation accommodation = tripDate.getAccommodation();
            if (accommodation != null) {
                accommodation.setAccommodationName(tripDateRequestDTO.getAccommodation().getAccommodationName());
                accommodation.setAccommodationLocation(tripDateRequestDTO.getAccommodation().getAccommodationLocation());
                accommodationRepository.save(accommodation);
            }
        }


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

    /**
     * 특정 세부 일정의 일부 정보를 수정합니다.
     * 주어진 세부 일정 ID와 부분적으로 수정할 정보를 사용하여 일정을 업데이트합니다.
     *
     * @param tripDateId         수정할 세부 일정의 ID
     * @param tripDateRequestDTO 수정할 정보가 담긴 DTO
     * @return 수정된 세부 일정 객체
     * @throws ComponentNotFoundException 세부 일정을 찾을 수 없는 경우 발생
     */
    @Transactional
    public TripDate updateTripDateDetailById(Long tripDateId, TripDateRequestDTO tripDateRequestDTO) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new ComponentNotFoundException("TripDate with ID " + tripDateId + " not found"));

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

    /**
     * 특정 세부 일정을 삭제합니다.
     * 주어진 세부 일정 ID를 사용하여 일정을 삭제합니다.
     *
     * @param tripDateId 삭제할 세부 일정의 ID
     * @throws ComponentNotFoundException 세부 일정을 찾을 수 없는 경우 발생
     */
    @Transactional
    public void deleteTripDateById(Long tripDateId) {
        TripDate tripDate = tripDateRepository.findById(tripDateId)
                .orElseThrow(() -> new ComponentNotFoundException("TripDate with ID " + tripDateId + " not found"));

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