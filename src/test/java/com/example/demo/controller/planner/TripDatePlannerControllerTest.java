package com.example.demo.controller.planner;

import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.dto.planner.tripDate.TripDateResponseDTO;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.service.planner.ComponentNotFoundException;
import com.example.demo.service.planner.TripDatePlannerService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = TripDatePlannerController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.example.demo.filter.CustomLogoutFilter.class})
}) // CustomLogoutFilter를 테스트에서 제외
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class TripDatePlannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripDatePlannerService tripDatePlannerService;

    private TripDate tripDate;
    private TripDateRequestDTO tripDateRequestDTO;

    @BeforeEach
    void setUp() {
        tripDate = new TripDate(1L, null, LocalDate.of(2024, 10, 10), 1, 5000L, null, Collections.emptyList());
        tripDateRequestDTO = new TripDateRequestDTO(LocalDate.of(2024, 10, 10), 1, 5000L, null, Collections.emptyList());
    }

    @Test
    @DisplayName("getTripDetails 성공 - 세부 일정을 조회할 수 있다")
    void getTripDetails_success() throws Exception {
        Mockito.when(tripDatePlannerService.getTripDates(anyLong())).thenReturn(Collections.singletonList(tripDate));

        mockMvc.perform(get("/planner/trip/{tripId}/detail", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripDate").value("2024-10-10"))
                .andExpect(jsonPath("$[0].budget").value(5000));
    }

    @Test
    @DisplayName("getTripDetails 실패 - 여행 세부 일정 조회 중 404 예외 발생")
    void getTripDetails_notFound() throws Exception {
        Mockito.when(tripDatePlannerService.getTripDates(anyLong()))
                .thenThrow(new ComponentNotFoundException("Trip details not found"));

        mockMvc.perform(get("/planner/trip/{tripId}/detail", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getTripDetails 실패 - 여행 세부 일정 조회 중 500 예외 발생")
    void getTripDetails_internalError() throws Exception {
        Mockito.when(tripDatePlannerService.getTripDates(anyLong()))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/planner/trip/{tripId}/detail", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("addMultipleTripDetails 성공 - 여러 세부 일정을 추가할 수 있다")
    void addMultipleTripDetails_success() throws Exception {
        TripDateResponseDTO tripDateResponseDTO = new TripDateResponseDTO(1L, LocalDate.of(2024, 10, 10), 1, 5000L, null, Collections.emptyList());

        Mockito.when(tripDatePlannerService.addTripDetail(anyLong(), any(TripDateRequestDTO.class)))
                .thenReturn(tripDate);

        mockMvc.perform(post("/planner/trip/{tripId}/detail", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"tripDate\": \"2024-10-10\", \"tripDay\": 1, \"budget\": 5000}]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].tripDate").value("2024-10-10"))
                .andExpect(jsonPath("$[0].budget").value(5000));
    }

    @Test
    @DisplayName("addMultipleTripDetails 실패 - 세부 일정 추가 중 500 예외 발생")
    void addMultipleTripDetails_internalError() throws Exception {
        Mockito.when(tripDatePlannerService.addTripDetail(anyLong(), any(TripDateRequestDTO.class)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/planner/trip/{tripId}/detail", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"tripDate\": \"2024-10-10\", \"tripDay\": 1, \"budget\": 5000}]"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("getTripDateById 성공 - 특정 세부 일정을 조회할 수 있다")
    void getTripDateById_success() throws Exception {
        Mockito.when(tripDatePlannerService.getTripDateById(anyLong(), anyLong())).thenReturn(tripDate);

        mockMvc.perform(get("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripDate").value("2024-10-10"))
                .andExpect(jsonPath("$.budget").value(5000));
    }

    @Test
    @DisplayName("getTripDateById 실패 - 세부 일정 조회 중 404 예외 발생")
    void getTripDateById_notFound() throws Exception {
        Mockito.when(tripDatePlannerService.getTripDateById(anyLong(), anyLong()))
                .thenThrow(new ComponentNotFoundException("Trip Date not found"));

        mockMvc.perform(get("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getTripDateById 실패 - 500 InternalServerError 발생")
    void getTripDateById_internalServerError() throws Exception {
        // 서버에서 예상치 못한 오류 발생
        Mockito.when(tripDatePlannerService.getTripDateById(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Internal Server Error"));

        // InternalServerError 상태를 기대
        mockMvc.perform(get("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("updateCompleteTripDateDetailById 성공 - 세부 일정을 수정할 수 있다")
    void updateCompleteTripDateDetailById_success() throws Exception {
        Mockito.when(tripDatePlannerService.updateCompleteTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenReturn(tripDate);

        mockMvc.perform(put("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDate\": \"2024-10-15\", \"tripDay\": 2, \"budget\": 10000 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripDate").value("2024-10-10"))
                .andExpect(jsonPath("$.budget").value(5000));
    }

    @Test
    @DisplayName("updateCompleteTripDateDetailById 실패 - 세부 일정 수정 중 404 예외 발생")
    void updateCompleteTripDateDetailById_notFound() throws Exception {
        Mockito.when(tripDatePlannerService.updateCompleteTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenThrow(new ComponentNotFoundException("Trip Date not found"));

        mockMvc.perform(put("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDate\": \"2024-10-15\", \"tripDay\": 2, \"budget\": 10000 }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateCompleteTripDateDetailById 실패 - 500 InternalServerError 발생")
    void updateCompleteTripDateDetailById_internalServerError() throws Exception {
        // 서버에서 예상치 못한 오류 발생
        Mockito.when(tripDatePlannerService.updateCompleteTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        // InternalServerError 상태를 기대
        mockMvc.perform(put("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDate\": \"2024-10-15\", \"tripDay\": 2, \"budget\": 10000 }"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @DisplayName("updateTripDateDetailById 성공 - 세부 일정을 부분적으로 수정할 수 있다")
    void updateTripDateDetailById_success() throws Exception {
        // Given: 수정된 TripDate 객체를 설정
        TripDate updatedTripDate = new TripDate();
        updatedTripDate.setTripDateId(1L);
        updatedTripDate.setTripDay(2);  // 수정된 tripDay 값
        updatedTripDate.setBudget(10000L);  // 수정된 예산 값

        // When: TripDatePlannerService가 업데이트된 TripDate를 반환하도록 Mock 설정
        Mockito.when(tripDatePlannerService.updateTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenReturn(updatedTripDate);

        // Then: 응답 상태와 수정된 값들을 검증
        mockMvc.perform(patch("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDay\": 2, \"budget\": 10000 }"))  // 수정할 값
                .andExpect(status().isOk())  // HTTP 상태 200 확인
                .andExpect(jsonPath("$.tripDay").value(2))  // tripDay가 2로 수정되었는지 확인
                .andExpect(jsonPath("$.budget").value(10000L));  // 예산이 10000으로 수정되었는지 확인
    }


    @Test
    @DisplayName("updateTripDateDetailById 실패 - 세부 일정 수정 중 404 예외 발생")
    void updateTripDateDetailById_notFound() throws Exception {
        Mockito.when(tripDatePlannerService.updateTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenThrow(new ComponentNotFoundException("Trip Date not found"));

        mockMvc.perform(patch("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDay\": 2, \"budget\": 10000 }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateTripDateDetailById 실패 - 500 InternalServerError 발생")
    void updateTripDateDetailById_internalServerError() throws Exception {
        Mockito.when(tripDatePlannerService.updateTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(patch("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDay\": 2, \"budget\": 10000 }"))
                .andExpect(status().isInternalServerError());
    }



    @Test
    @DisplayName("deleteTripDateById 성공 - 세부 일정을 삭제할 수 있다")
    void deleteTripDateById_success() throws Exception {
        mockMvc.perform(delete("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteTripDateById 성공 - 204 NoContent 상태를 반환한다")
    void deleteTripDateById_noContent() throws Exception {
        mockMvc.perform(delete("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteTripDateById 실패 - 세부 일정 삭제 중 404 예외 발생")
    void deleteTripDateById_notFound() throws Exception {
        Mockito.doThrow(new ComponentNotFoundException("Trip Date not found"))
                .when(tripDatePlannerService).deleteTripDateById(anyLong());

        mockMvc.perform(delete("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L))
                .andExpect(status().isNotFound());
    }
}
