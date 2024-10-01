package com.example.demo.dto.planner.tripDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 숙소 정보를 담고 있는 응답용 DTO입니다.
 * 서버가 클라이언트에게 숙소 정보를 제공할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationResponseDTO {
    private String accommodationName;
    private String accommodationLocation;
}