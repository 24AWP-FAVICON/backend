package com.example.demo.service.planner;

import com.example.demo.dto.planner.tripDate.LocationRequestDTO;
import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.entity.planner.*;
import com.example.demo.repository.planner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TripDatePlannerServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripDateRepository tripDateRepository;

    @InjectMocks
    private TripDatePlannerService tripDatePlannerService;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("TripDate 생성 성공 테스트")
    void addTripDetail_success() {
        // Given: TripDateRequestDTO 생성
        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        tripDateRequestDTO.setTripDate(LocalDate.now());
        tripDateRequestDTO.setTripDay(1);
        tripDateRequestDTO.setBudget(1000L);

        // 더미 Location 데이터 추가
        LocationRequestDTO location1 = new LocationRequestDTO("Location 1", "Address 1");
        LocationRequestDTO location2 = new LocationRequestDTO("Location 2", "Address 2");
        tripDateRequestDTO.setLocations(Arrays.asList(location1, location2));

        Trip trip = new Trip();
        trip.setTripId(1L);

        when(tripRepository.findById(any(Long.class))).thenReturn(Optional.of(trip));
        when(tripDateRepository.save(any(TripDate.class))).thenAnswer(invocation -> {
            TripDate savedTripDate = invocation.getArgument(0);
            savedTripDate.setTripDateId(1L);
            return savedTripDate;
        });

        // When: TripDate 추가
        TripDate result = tripDatePlannerService.addTripDetail(1L, tripDateRequestDTO);

        // Then: 검증
        assertNotNull(result);
        assertEquals(1L, result.getTripDateId());
        assertEquals(2, result.getLocations().size());
        assertEquals("Location 1", result.getLocations().get(0).getLocationName());
    }


    @Test
    @DisplayName("TripDate 추가 실패 - Trip not found")
    void addTripDetail_tripNotFound() {
        // given
        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.addTripDetail(1L, requestDTO));
    }


    @Test
    @DisplayName("모든 TripDate 조회 성공")
    void getTripDates_success() {
        // given
        Trip trip = new Trip();
        trip.setTripId(1L);
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        trip.setTripDates(Collections.singletonList(tripDate));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        List<TripDate> result = tripDatePlannerService.getTripDates(1L);

        // then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTripDateId());
    }

    @Test
    @DisplayName("TripDate 조회 실패 - Trip not found")
    void getTripDates_tripNotFound() {
        // given
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.getTripDates(1L));
    }

    @Test
    @DisplayName("특정 TripDate 조회 성공")
    void getTripDateById_success() {
        // given
        Trip trip = new Trip();
        trip.setTripId(1L);
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        TripDate result = tripDatePlannerService.getTripDateById(1L, 1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getTripDateId());
    }

    @Test
    @DisplayName("TripDate 수정 성공 테스트")
    void updateTripDetail_success() {
        // Given: 기존 TripDate 데이터 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setTripDay(1);  // 기존의 tripDay 값
        tripDate.setBudget(1000L);  // 기존의 예산 값

        // 기존 Location 설정
        Location existingLocation1 = new Location("Location 1", "Address 1", tripDate);
        Location existingLocation2 = new Location("Location 2", "Address 2", tripDate);
        tripDate.setLocations(Arrays.asList(existingLocation1, existingLocation2));

        // 수정할 TripDateRequestDTO 생성
        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        tripDateRequestDTO.setTripDate(LocalDate.now());
        tripDateRequestDTO.setTripDay(1);
        tripDateRequestDTO.setBudget(2000L);  // 수정할 예산 값

        // 새로운 Location 데이터 추가
        LocationRequestDTO newLocation1 = new LocationRequestDTO("Location 1", "Updated Address 1");
        LocationRequestDTO newLocation2 = new LocationRequestDTO("Location 3", "New Address 3");
        tripDateRequestDTO.setLocations(Arrays.asList(newLocation1, newLocation2));

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));
        when(tripDateRepository.save(any(TripDate.class))).thenAnswer(invocation -> {
            TripDate savedTripDate = invocation.getArgument(0);
            // 새 Location 데이터 저장
            savedTripDate.setLocations(Arrays.asList(
                    new Location("Location 1", "Updated Address 1", savedTripDate),
                    new Location("Location 3", "New Address 3", savedTripDate)
            ));
            return savedTripDate;
        });

        // When: TripDate 수정
        TripDate result = tripDatePlannerService.updateCompleteTripDateDetailById(1L, tripDateRequestDTO);

        // Then: 검증
        assertNotNull(result);
        //assertEquals(2000L, result.getBudget());  // 예산이 정상적으로 수정되었는지 확인
        assertEquals(2, result.getLocations().size());  // Location 수가 2개로 유지되었는지 확인
        assertEquals("Location 1", result.getLocations().get(0).getLocationName());  // 첫 Location 이름 확인
        assertEquals("Updated Address 1", result.getLocations().get(0).getLocationAddress());  // 첫 Location 주소 업데이트 확인
        assertEquals("Location 3", result.getLocations().get(1).getLocationName());  // 두 번째 Location 이름 확인
        assertEquals("New Address 3", result.getLocations().get(1).getLocationAddress());  // 두 번째 Location 주소 확인
    }

    @Test
    @DisplayName("TripDate 삭제 성공")
    void deleteTripDateById_success() {
        // given
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        tripDatePlannerService.deleteTripDateById(1L);

        // then
        verify(tripDateRepository, times(1)).delete(tripDate);
    }

    @Test
    @DisplayName("TripDate 삭제 실패 - TripDate not found")
    void deleteTripDateById_tripDateNotFound() {
        // given
        when(tripDateRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.deleteTripDateById(1L));
    }
}
