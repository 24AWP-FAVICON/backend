package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDateResponseDTO {
    private Long tripDateId;
    private LocalDate tripDate;
    private int tripDay;
    private Long budget;
    private AccommodationDetailsDTO accommodation;
    private List<LocationDetailsDTO> locations;

}
