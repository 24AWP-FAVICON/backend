package com.example.demo.dto.planner.tripDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 계획 내 특정 일자의 세부 일정을 생성하기 위한 요청 데이터를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDateRequestDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tripDate;

    private Integer tripDay;
    private Long budget;

    private AccommodationRequestDTO accommodation;
    private List<LocationRequestDTO> locations;

}
