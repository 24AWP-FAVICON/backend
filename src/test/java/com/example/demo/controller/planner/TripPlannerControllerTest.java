package com.example.demo.controller.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.dto.planner.trip.TripResponseDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.exception.InvalidUserException;
import com.example.demo.service.planner.ComponentNotFoundException;
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
    @DisplayName("getAllTrips 실패 - 여행 계획이 없을 때 204 NO_CONTENT 응답을 반환한다")
    void getAllTrips_noContent() throws Exception {
        // 여행 계획이 없을 때 빈 리스트를 반환하도록 모킹
        Mockito.when(tripPlannerService.getAllTrips()).thenReturn(Collections.emptyList());

        // NO_CONTENT 상태를 반환하는지 검증
        mockMvc.perform(get("/planner/trip")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("getAllTrips 실패 - 500 내부 서버 오류 발생")
    void getAllTrips_internalServerError() throws Exception {
        Mockito.when(tripPlannerService.getAllTrips())
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/planner/trip")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
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
    @DisplayName("getTripById 실패 - 404 예외 발생")
    void getTripById_notFound() throws Exception {
        Mockito.when(tripPlannerService.getTripById(anyLong()))
                .thenThrow(new ComponentNotFoundException("Trip not found"));

        mockMvc.perform(get("/planner/trip/{tripId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getTripById 실패 - 내부 서버 오류 발생 시 500 INTERNAL_SERVER_ERROR 응답을 반환한다")
    void getTripById_internalServerError() throws Exception {
        // TripPlannerService에서 예기치 않은 예외가 발생하도록 모킹
        Mockito.when(tripPlannerService.getTripById(anyLong()))
                .thenThrow(new RuntimeException("Internal Server Error"));

        // INTERNAL_SERVER_ERROR 상태를 반환하는지 검증
        mockMvc.perform(get("/planner/trip/{tripId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
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
    @DisplayName("addNewTrip 실패 - 500 예외 발생")
    void addNewTrip_internalServerError() throws Exception {
        Mockito.when(tripPlannerService.createTrip(any(TripRequestDTO.class)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/planner/trip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Trip to Korea\", \"tripArea\": \"Seoul\", " +
                                "\"startDate\": \"2024-10-10\", \"endDate\": \"2024-10-15\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("updateTripById 성공 - 특정 여행 계획을 수정할 수 있다")
    void updateTripById_success() throws Exception {
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
    @DisplayName("updateTripById 실패 - 404 예외 발생")
    void updateTripById_notFound() throws Exception {
        Mockito.when(tripPlannerService.updateTrip(anyLong(), any(Trip.class)))
                .thenThrow(new ComponentNotFoundException("Trip not found"));

        mockMvc.perform(put("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", \"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateTripById 실패 - 404 예외 발생")
    void updateTripById_internalServerError() throws Exception { //TODO:
        Mockito.when(tripPlannerService.updateTrip(anyLong(), any(Trip.class)))
                .thenThrow(new ComponentNotFoundException("Trip not found"));

        mockMvc.perform(put("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", \"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("partialUpdateTripById 성공 - 여행 계획의 일부를 수정할 수 있다")
    void partialUpdateTripById_success() throws Exception {
        Trip updatedTrip = new Trip(1L, Collections.emptyList(), "Updated Trip", "Busan",
                LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10), 100000L, null);

        Mockito.when(tripPlannerService.partialUpdateTrip(anyLong(), any(TripRequestDTO.class))).thenReturn(updatedTrip);

        mockMvc.perform(patch("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", " +
                                "\"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripName").value("Updated Trip"))
                .andExpect(jsonPath("$.tripArea").value("Busan"));
    }

    @Test
    @DisplayName("partialUpdateTripById 실패 - 여행 계획이 없는 경우 404 예외 발생")
    void partialUpdateTripById_notFound() throws Exception {
        Mockito.when(tripPlannerService.partialUpdateTrip(anyLong(), any(TripRequestDTO.class)))
                .thenThrow(new ComponentNotFoundException("Trip not found"));

        mockMvc.perform(patch("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", " +
                                "\"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("partialUpdateTripById 실패 - 내부 서버 오류 발생")
    void partialUpdateTripById_internalServerError() throws Exception {
        Mockito.when(tripPlannerService.partialUpdateTrip(anyLong(), any(TripRequestDTO.class)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(patch("/planner/trip/{tripId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripName\": \"Updated Trip\", \"tripArea\": \"Busan\", " +
                                "\"startDate\": \"2024-11-01\", \"endDate\": \"2024-11-10\", \"budget\": 100000, \"participantIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @DisplayName("deleteTripById 성공 - 특정 여행 계획을 삭제할 수 있다")
    void deleteTripById_success() throws Exception {
        mockMvc.perform(delete("/planner/trip/{tripId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteTripById 성공 - Trip이 정상적으로 삭제되면 204 NO_CONTENT 응답을 반환한다")
    void deleteTripById_noContent() throws Exception {
        // given: TripPlannerService의 deleteTripById()가 정상 동작하도록 설정
        Mockito.doNothing().when(tripPlannerService).deleteTripById(anyLong());

        // when / then: NO_CONTENT 상태가 반환되는지 검증
        mockMvc.perform(delete("/planner/trip/{tripId}", 1L))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("deleteTripById 실패 - 404 예외 발생")
    void deleteTripById_notFound() throws Exception {
        Mockito.doThrow(new ComponentNotFoundException("Trip not found"))
                .when(tripPlannerService).deleteTripById(anyLong());

        mockMvc.perform(delete("/planner/trip/{tripId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteTripById 실패 - 500 내부 서버 오류 발생")
    void deleteTripById_internalServerError() throws Exception {
        Mockito.doThrow(new RuntimeException("Internal Server Error"))
                .when(tripPlannerService).deleteTripById(anyLong());

        mockMvc.perform(delete("/planner/trip/{tripId}", 1L))
                .andExpect(status().isInternalServerError());
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

    @Test
    @DisplayName("shareTripPlanWithUser 실패 - 400 예외 발생")
    void shareTripPlanWithUser_invalidUser() throws Exception {
        Mockito.doThrow(new InvalidUserException("Invalid user"))
                .when(tripPlannerService).shareTripPlanWithUser(anyLong(), any());

        mockMvc.perform(post("/planner/trip/{tripId}/share", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userGoogleIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid user"));
    }

    @Test
    @DisplayName("shareTripPlanWithUser 실패 - 여행 계획이 없는 경우 404 예외 발생")
    void shareTripPlanWithUser_notFound() throws Exception {
        // given: 여행 계획을 찾을 수 없는 상황을 모킹
        Mockito.doThrow(new ComponentNotFoundException("Trip not found"))
                .when(tripPlannerService).shareTripPlanWithUser(anyLong(), any());

        // when / then: 404 예외가 발생하는지 검증
        mockMvc.perform(post("/planner/trip/{tripId}/share", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userGoogleIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Trip not found"));
    }

    @Test
    @DisplayName("shareTripPlanWithUser 실패 - 유효하지 않은 사용자로 인해 400 BadRequest를 반환한다")
    void shareTripPlanWithUser_BadRequest() throws Exception {
        // given: 유효하지 않은 사용자 예외를 발생시키도록 설정
        Mockito.doThrow(new InvalidUserException("Invalid user"))
                .when(tripPlannerService).shareTripPlanWithUser(anyLong(), any());

        // when / then: BadRequest 상태와 메시지를 확인
        mockMvc.perform(post("/planner/trip/{tripId}/share", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userGoogleIds\": [\"invalidUser1\", \"invalidUser2\"] }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid user"));
    }

    @Test
    @DisplayName("shareTripPlanWithUser 실패 - 서버 오류로 인해 500 InternalServerError를 반환한다")
    void shareTripPlanWithUser_InternalServerError() throws Exception {
        // given: 서버 내부 오류를 발생시키도록 설정
        Mockito.doThrow(new RuntimeException("Internal Server Error"))
                .when(tripPlannerService).shareTripPlanWithUser(anyLong(), any());

        // when / then: InternalServerError 상태와 메시지를 확인
        mockMvc.perform(post("/planner/trip/{tripId}/share", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userGoogleIds\": [\"1\", \"2\"] }"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error sharing trip plan: Internal Server Error"));
    }


}
