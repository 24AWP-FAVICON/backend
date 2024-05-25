package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccommodationDetailsDTO {

    private String accommodationName;
    private String accommodationLocation;

    @JsonIgnore // 순환 참조 방지
    private TripDateDetailsDTO tripDate;
}
