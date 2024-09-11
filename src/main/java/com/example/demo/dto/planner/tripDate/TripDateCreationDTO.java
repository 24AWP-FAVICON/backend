package com.example.demo.dto.planner.tripDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDateCreationDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tripDate;

    private Integer tripDay;
    private Long budget;

    @NotNull
    private AccommodationCreationDTO accommodation;
    private List<LocationCreationDTO> locations;

}
