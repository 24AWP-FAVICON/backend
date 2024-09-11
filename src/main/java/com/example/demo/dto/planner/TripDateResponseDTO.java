package com.example.demo.dto.planner;

import com.example.demo.dto.planner.AccommodationDetailsDTO;
import com.example.demo.dto.planner.LocationDetailsDTO;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

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
