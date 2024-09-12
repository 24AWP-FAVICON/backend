package com.example.demo.dto.planner.tripDate;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 계획 내 특정 일자의 세부 일정을 응답하기 위한 DTO입니다.
 * 서버가 클라이언트에게 일자별 세부 일정을 제공할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TripDateResponseDTO {

    private Long tripDateId;
    private LocalDate tripDate;
    private int tripDay;
    private Long budget;
    private AccommodationResponseDTO accommodation;
    private List<LocationResponseDTO> locations;
}
