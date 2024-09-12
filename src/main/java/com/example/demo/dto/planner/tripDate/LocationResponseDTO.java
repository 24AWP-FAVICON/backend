package com.example.demo.dto.planner.tripDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 여행지 정보를 담고 있는 응답용 DTO입니다.
 * 서버가 클라이언트에게 특정 위치 정보를 제공할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDTO {
    private String locationName;
    private String locationAddress;
}