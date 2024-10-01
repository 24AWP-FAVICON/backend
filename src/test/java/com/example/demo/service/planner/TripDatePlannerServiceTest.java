package com.example.demo.service.planner;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("TripDate 추가 성공")
    void addTripDetail_Success() {
        // given
        Trip trip = new Trip();
        trip.setTripId(1L);

        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        requestDTO.setTripDate(LocalDate.now());
        requestDTO.setTripDay(1);
        requestDTO.setBudget(10000L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDateRepository.save(any(TripDate.class))).thenReturn(new TripDate());

        // when
        TripDate result = tripDatePlannerService.addTripDetail(1L, requestDTO);

        // then
        assertNotNull(result);
        verify(tripDateRepository, times(1)).save(any(TripDate.class));
    }

    @Test
    @DisplayName("TripDate 추가 실패 - Trip not found")
    void addTripDetail_Fail_TripNotFound() {
        // given
        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.addTripDetail(1L, requestDTO));
    }


    @Test
    @DisplayName("모든 TripDate 조회 성공")
    void getTripDates_Success() {
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
    void getTripDates_Fail_TripNotFound() {
        // given
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.getTripDates(1L));
    }

    @Test
    @DisplayName("특정 TripDate 조회 성공")
    void getTripDateById_Success() {
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
    @DisplayName("TripDate 수정 성공")
    void updateCompleteTripDateDetailById_Success() {
        // given
        TripDate tripDate = new TripDate();
        tripDate.setTripDateId(1L);

        TripDateRequestDTO requestDTO = new TripDateRequestDTO();
        requestDTO.setTripDate(LocalDate.now());
        requestDTO.setTripDay(2);
        requestDTO.setBudget(20000L);

        when(tripDateRepository.findById(1L)).thenReturn(Optional.of(tripDate));

        // when
        TripDate result = tripDatePlannerService.updateCompleteTripDateDetailById(1L, requestDTO);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTripDay());
        assertEquals(20000L, result.getBudget());
    }

    @Test
    @DisplayName("TripDate 삭제 성공")
    void deleteTripDateById_Success() {
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
    void deleteTripDateById_Fail_TripDateNotFound() {
        // given
        when(tripDateRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ComponentNotFoundException.class, () -> tripDatePlannerService.deleteTripDateById(1L));
    }
}
