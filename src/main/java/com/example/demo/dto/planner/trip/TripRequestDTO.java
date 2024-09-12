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
