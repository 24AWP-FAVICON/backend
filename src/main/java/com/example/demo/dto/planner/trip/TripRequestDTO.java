package com.example.demo.dto.planner.trip;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 계획 생성을 위한 요청 데이터를 담고 있는 DTO입니다.
 * 사용자가 여행 계획을 생성할 때 필요한 필드들을 포함하고 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripRequestDTO {

    @NotNull(message = "Trip name must not be null")
    private String tripName;

    @NotNull(message = "Trip area must not be null")
    private String tripArea;

    @NotNull(message = "Start date must not be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    private Long budget;

    @NotNull(message = "Participants must not be null")
    @NotEmpty(message = "Participant list must not be empty")
    private List<String> participantIds; // 참여자 ID
}
