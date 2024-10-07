package com.example.demo.controller.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.dto.planner.trip.TripResponseDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.service.planner.TripPlannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TripPlannerController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.example.demo.filter.CustomLogoutFilter.class})
}) // CustomLogoutFilter를 테스트에서 제외
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class TripPlannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripPlannerService tripPlannerService;

    private Trip trip;
    private TripRequestDTO tripRequestDTO;
    private TripResponseDTO tripResponseDTO;

    @BeforeEach
    void setUp() {
        trip = new Trip(1L, Collections.emptyList(), "Trip to Korea", "Seoul",
                LocalDate.now(), LocalDate.now().plusDays(5), 100000L, null);

        tripRequestDTO = new TripRequestDTO("Trip to Korea", "Seoul",
                LocalDate.now(), LocalDate.now().plusDays(5), 100000L, Arrays.asList("1", "2"));

        tripResponseDTO = TripResponseDTO.fromEntity(trip);
    }

    @Test
    @DisplayName("getAllTrips 성공 - 여행 계획을 조회할 수 있다")
    void getAllTrips_success() throws Exception {
        List<TripResponseDTO> trips = Arrays.asList(tripResponseDTO);

        Mockito.when(tripPlannerService.getAllTrips()).thenReturn(Arrays.asList(trip));

        mockMvc.perform(get("/planner/trip")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(trips.size()))
                .andExpect(jsonPath("$[0].tripName").value(trip.getTripName()));
    }

    @Test
    @DisplayName("getTripById 성공 - 여행 ID로 특정 여행 계획을 조회할 수 있다")
    void getTripById_success() throws Exception {
        Mockito.when(tripPlannerService.getTripById(anyLong())).thenReturn(trip);

        mockMvc.perform(get("/planner/trip/{tripId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripName").value(trip.getTripName()));
    }

    @Test
    @DisplayName("addNewTrip 성공 - 새로운 여행 계획을 생성할 수 있다")
    void addNewTrip_success() throws Exception {
        Mockito.when(tripPlannerService.createTrip(any(TripRequestDTO.class))).thenReturn(trip);

        mockMvc.perform(post("/planner/trip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Trip to Korea\", \"tripArea\": \"Seoul\", " +
                                "\"startDate\": \"2024-10-10\", \"endDate\": \"2024-10-15\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tripName").value(trip.getTripName()));
    }

    @Test
    @DisplayName("updateTripById 성공 - 특정 여행 계획을 수정할 수 있다")
    void updateTripById_success() throws Exception {
        // 수정된 Trip 객체를 반환하도록 목 처리
        Trip updatedTrip = new Trip(1L, Collections.emptyList(), "Updated Trip", "Busan",
                LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10), 100000L, null);

        Mockito.when(tripPlannerService.updateTrip(anyLong(), any(Trip.class))).thenReturn(updatedTrip);

        mockMvc.perform(put("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", \"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripName").value("Updated Trip"))
                .andExpect(jsonPath("$.tripArea").value("Busan"));
    }

    @Test
    @DisplayName("partialUpdateTripById 성공 - 특정 여행 계획을 부분 수정할 수 있다")
    void partialUpdateTripById_success() throws Exception {
        // 부분적으로 수정된 Trip 객체를 반환하도록 목 처리
        Trip partiallyUpdatedTrip = new Trip(1L, Collections.emptyList(), "Updated Trip2", "Busan",
                LocalDate.of(2024, 10, 7), LocalDate.of(2024, 10, 12), 100000L, null);

        // partialUpdateTrip 메서드를 목 처리하여 부분적으로 수정된 Trip 객체 반환
        Mockito.when(tripPlannerService.partialUpdateTrip(anyLong(), any(TripRequestDTO.class))).thenReturn(partiallyUpdatedTrip);

        mockMvc.perform(patch("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip2\", \"tripArea\": \"Busan\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripName").value("Updated Trip2"))
                .andExpect(jsonPath("$.tripArea").value("Busan"));
    }


    @Test
    @DisplayName("deleteTripById 성공 - 특정 여행 계획을 삭제할 수 있다")
    void deleteTripById_success() throws Exception {
        mockMvc.perform(delete("/planner/trip/{tripId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("shareTripPlanWithUser 성공 - 여행 계획을 다른 사용자와 공유할 수 있다")
    void shareTripPlanWithUser_success() throws Exception {
        Mockito.doNothing().when(tripPlannerService).shareTripPlanWithUser(anyLong(), any());

        mockMvc.perform(post("/planner/trip/{tripId}/share", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userGoogleIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isOk())
                .andExpect(content().string("User added to the trip successfully"));
    }
}
