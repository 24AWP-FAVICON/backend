package com.example.demo.dto.planner.tripDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 숙소 정보를 담고 있는 요청용 DTO입니다.
 * 사용자가 여행 계획에 숙소 정보를 추가할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationRequestDTO {
    private String accommodationName;
    private String accommodationLocation;
}
