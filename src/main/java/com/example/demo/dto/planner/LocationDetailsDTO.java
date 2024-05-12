package com.example.demo.dto.planner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

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
