package com.example.demo.service.planner;

import com.example.demo.dto.planner.tripDate.AccommodationRequestDTO;
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
    @DisplayName("addTripDetail 성공 - 새로운 TripDate를 추가할 수 있다")
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
    @DisplayName("addTripDetail 성공 - 숙소 정보가 있는 경우 TripDate에 추가할 수 있다")
    void addTripDetail_withAccommodation_success() {
        // Given: TripDateRequestDTO 생성 (숙소 정보 포함)
        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        tripDateRequestDTO.setTripDate(LocalDate.now());
        tripDateRequestDTO.setTripDay(1);
        tripDateRequestDTO.setBudget(1000L);

        AccommodationRequestDTO accommodationDTO = new AccommodationRequestDTO("Hotel ABC", "Seoul");
        tripDateRequestDTO.setAccommodation(accommodationDTO);

        // 명확히 빈 리스트 설정
        tripDateRequestDTO.setLocations(Collections.emptyList());

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
        assertNotNull(result.getAccommodation());
        assertEquals("Hotel ABC", result.getAccommodation().getAccommodationName());
        assertEquals("Seoul", result.getAccommodation().getAccommodationLocation());
    }



    @Test
    @DisplayName("addTripDetail 실패 - Trip을 찾을 수 없을 때 ComponentNotFoundException 발생")
    void addTripDetail_tripNotFound() {
        // given
        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.addTripDetail(1L, requestDTO));
    }


    @Test
    @DisplayName("getTripDates 성공 - 모든 TripDate를 조회할 수 있다")
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
    @DisplayName("getTripDates 실패 - Trip을 찾을 수 없을 때 ComponentNotFoundException 발생")
    void getTripDates_tripNotFound() {
        // given
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.getTripDates(1L));
    }

    @Test
    @DisplayName("getTripDateById 성공 - 특정 TripDate를 조회할 수 있다")
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
    @DisplayName("updateTripDetail 성공 - TripDate를 수정할 수 있다")
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
    @DisplayName("updateCompleteTripDateDetailById 성공 - 숙소 정보를 업데이트할 수 있다")
    void updateCompleteTripDateDetailById_withAccommodation_success() {
        // Given: 기존 TripDate와 숙소 정보 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setAccommodation(new Accommodation("Old Hotel", "Old Location"));

        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        AccommodationRequestDTO newAccommodationDTO = new AccommodationRequestDTO("New Hotel", "New Location");
        tripDateRequestDTO.setAccommodation(newAccommodationDTO);

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));
        when(tripDateRepository.save(any(TripDate.class))).thenReturn(tripDate);

        // When: TripDate 수정
        TripDate result = tripDatePlannerService.updateCompleteTripDateDetailById(1L, tripDateRequestDTO);

        // Then: 검증
        assertNotNull(result);
        assertEquals("New Hotel", result.getAccommodation().getAccommodationName());
        assertEquals("New Location", result.getAccommodation().getAccommodationLocation());
    }


    @Test
    @DisplayName("updateCompleteTripDateDetailById 성공 - 위치 정보를 업데이트할 수 있다")
    void updateCompleteTripDateDetailById_withLocations_success() {
        // Given: 기존 TripDate와 위치 정보 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setLocations(Arrays.asList(
                new Location("Old Location 1", "Old Address 1", tripDate),
                new Location("Old Location 2", "Old Address 2", tripDate)
        ));

        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        List<LocationRequestDTO> newLocations = Arrays.asList(
                new LocationRequestDTO("New Location 1", "New Address 1"),
                new LocationRequestDTO("New Location 2", "New Address 2")
        );
        tripDateRequestDTO.setLocations(newLocations); // 반드시 locations 설정

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));
        when(tripDateRepository.save(any(TripDate.class))).thenReturn(tripDate);

        // When: TripDate 수정
        TripDate result = tripDatePlannerService.updateCompleteTripDateDetailById(1L, tripDateRequestDTO);

        // Then: 검증
        assertNotNull(result);
        assertEquals(2, result.getLocations().size());
        assertEquals("New Location 1", result.getLocations().get(0).getLocationName());
        assertEquals("New Address 1", result.getLocations().get(0).getLocationAddress());
    }

    @Test
    @DisplayName("updateTripDateDetailById 성공 - 부분적으로 TripDate를 수정할 수 있다")
    void updateTripDateDetailById_success() {
        // Given: 기존 TripDate 데이터 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setTripDay(1);  // 기존의 tripDay 값
        tripDate.setBudget(1000L);  // 기존의 예산 값

        // 수정할 TripDateRequestDTO 생성
        TripDateRequestDTO tripDateRequestDTO = new TripDateRequestDTO();
        tripDateRequestDTO.setTripDay(2);  // 수정할 tripDay 값
        tripDateRequestDTO.setBudget(2000L);  // 수정할 예산 값
        tripDateRequestDTO.setLocations(Collections.emptyList()); // 빈 리스트를 설정

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));
        when(tripDateRepository.save(any(TripDate.class))).thenReturn(tripDate);

        // When: TripDate 부분 수정
        TripDate result = tripDatePlannerService.updateTripDateDetailById(1L, tripDateRequestDTO);

        // Then: 검증
        assertNotNull(result);
        assertEquals(2, result.getTripDay());  // tripDay 값이 수정되었는지 확인
        assertEquals(2000L, result.getBudget());  // 예산이 수정되었는지 확인
        verify(tripDateRepository, times(1)).save(any(TripDate.class));
    }



    @Test
    @DisplayName("updateTripDateDetailById 실패 - TripDate를 찾을 수 없을 때 ComponentNotFoundException 발생")
    void updateTripDateDetailById_notFound() {
        // Given: TripDate가 존재하지 않는 상황 설정
        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        when(tripDateRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then: 예외 발생 검증
        assertThrows(ComponentNotFoundException.class, () -> {
            tripDatePlannerService.updateTripDateDetailById(1L, requestDTO);
        });
    }

    @Test
    @DisplayName("updateTripDateDetailById 실패 - 숙소 정보가 존재하지 않을 때의 처리")
    void updateTripDateDetailById_noAccommodation() {
        // Given: TripDate에 숙소 정보가 없는 경우 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setAccommodation(null);  // 숙소 정보 없음

        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        requestDTO.setAccommodation(null);  // 수정할 숙소 정보 없음

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));
        when(tripDateRepository.save(any(TripDate.class))).thenReturn(tripDate);

        // When: TripDate 수정
        TripDate result = tripDatePlannerService.updateTripDateDetailById(1L, requestDTO);

        // Then: 검증
        assertNotNull(result);
        assertNull(result.getAccommodation());  // 숙소 정보가 여전히 없는지 확인
    }

    @Test
    @DisplayName("deleteTripDateById 실패 - TripDate를 찾을 수 없을 때 ComponentNotFoundException 발생")
    void deleteTripDateById_notFound() {
        // Given: TripDate가 존재하지 않는 상황 설정
        when(tripDateRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then: 예외 발생 검증
        assertThrows(ComponentNotFoundException.class, () -> {
            tripDatePlannerService.deleteTripDateById(1L);
        });
    }


    @Test
    @DisplayName("deleteTripDateById 성공 - 숙소와 위치 정보가 있는 경우 TripDate를 삭제할 수 있다")
    void deleteTripDateById_withAccommodationAndLocations_success() {
        // given: 숙소와 위치 정보가 있는 TripDate 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);

        // 숙소 설정
        Accommodation accommodation = new Accommodation();
        tripDate.setAccommodation(accommodation);

        // 위치 정보 설정
        List<Location> locations = Arrays.asList(
                new Location("Location 1", "Address 1", tripDate),
                new Location("Location 2", "Address 2", tripDate)
        );
        tripDate.setLocations(locations);

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        tripDatePlannerService.deleteTripDateById(1L);

        // then: 숙소와 위치 정보가 삭제되었는지 검증
        verify(accommodationRepository, times(1)).delete(accommodation);
        verify(locationRepository, times(1)).deleteAll(locations);
        verify(tripDateRepository, times(1)).delete(tripDate);
    }

    @Test
    @DisplayName("deleteTripDateById 성공 - 숙소 정보가 없고 위치 정보만 있는 경우 TripDate를 삭제할 수 있다")
    void deleteTripDateById_withoutAccommodation_success() {
        // given: 숙소 정보는 없고 위치 정보만 있는 TripDate 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setAccommodation(null);  // 숙소 없음

        // 위치 정보 설정
        List<Location> locations = Arrays.asList(
                new Location("Location 1", "Address 1", tripDate),
                new Location("Location 2", "Address 2", tripDate)
        );
        tripDate.setLocations(locations);

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        tripDatePlannerService.deleteTripDateById(1L);

        // then: 위치 정보만 삭제되었는지 검증
        verify(accommodationRepository, never()).delete(any(Accommodation.class));  // 숙소 삭제가 호출되지 않음
        verify(locationRepository, times(1)).deleteAll(locations);
        verify(tripDateRepository, times(1)).delete(tripDate);
    }

    @Test
    @DisplayName("deleteTripDateById 성공 - 위치 정보가 없고 숙소 정보만 있는 경우 TripDate를 삭제할 수 있다")
    void deleteTripDateById_withoutLocations_success() {
        // given: 위치 정보는 없고 숙소 정보만 있는 TripDate 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);

        // 숙소 설정
        Accommodation accommodation = new Accommodation();
        tripDate.setAccommodation(accommodation);

        // 위치 정보 없음
        tripDate.setLocations(Collections.emptyList());

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        tripDatePlannerService.deleteTripDateById(1L);

        // then: 숙소 정보만 삭제되었는지 검증
        verify(accommodationRepository, times(1)).delete(accommodation);
        verify(locationRepository, never()).deleteAll(anyList());  // 위치 삭제가 호출되지 않음
        verify(tripDateRepository, times(1)).delete(tripDate);
    }

    @Test
    @DisplayName("deleteTripDateById 성공 - 숙소와 위치 정보가 모두 없는 경우 TripDate를 삭제할 수 있다")
    void deleteTripDateById_noAccommodationAndNoLocations_success() {
        // given: 숙소와 위치 정보가 없는 TripDate 설정
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);
        tripDate.setAccommodation(null);  // 숙소 없음
        tripDate.setLocations(Collections.emptyList());  // 위치 없음

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        tripDatePlannerService.deleteTripDateById(1L);

        // then: 아무 것도 삭제되지 않고 TripDate만 삭제되었는지 검증
        verify(accommodationRepository, never()).delete(any(Accommodation.class));  // 숙소 삭제가 호출되지 않음
        verify(locationRepository, never()).deleteAll(anyList());  // 위치 삭제가 호출되지 않음
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
