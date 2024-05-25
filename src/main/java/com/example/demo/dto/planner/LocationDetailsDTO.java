package com.example.demo.dto.planner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationDetailsDTO {

    private String locationName;
    private String locationAddress;

    @JsonIgnore // 순환 참조 방지
    private TripDateDetailsDTO tripDate;

}
