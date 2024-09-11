package com.example.demo.dto.planner.tripDate;

import lombok.*;

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
