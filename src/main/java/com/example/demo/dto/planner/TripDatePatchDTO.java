package com.example.demo.dto.planner;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDatePatchDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tripDate;

    private Integer tripDay;
    private Long budget;

    @NotNull
    private AccommodationResponseDTO accommodation;
    private List<LocationResponseDTO> locations;
}

