package com.example.demo.controller.planner;

import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.dto.planner.tripDate.TripDateResponseDTO;
import com.example.demo.entity.planner.TripDate;
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
    @DisplayName("updateTripDateDetailById 성공 - 세부 일정을 부분 수정할 수 있다")
    void updateTripDateDetailById_success() throws Exception {
        Mockito.when(tripDatePlannerService.updateTripDateDetailById(anyLong(), any(TripDateRequestDTO.class)))
                .thenReturn(tripDate);

        mockMvc.perform(patch("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tripDay\": 2 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripDay").value(1));
    }

    @Test
    @DisplayName("deleteTripDateById 성공 - 세부 일정을 삭제할 수 있다")
    void deleteTripDateById_success() throws Exception {
        mockMvc.perform(delete("/planner/trip/{tripId}/detail/{tripDateId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }
}
