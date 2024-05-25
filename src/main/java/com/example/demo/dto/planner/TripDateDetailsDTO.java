package com.example.demo.dto.planner;

import com.example.demo.dto.planner.AccommodationDetailsDTO;
import com.example.demo.dto.planner.LocationDetailsDTO;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TripDateDetailsDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tripDate;

    private Integer tripDay;
    private Long budget;

    @NotNull
    private AccommodationDetailsDTO accommodation;
    private List<LocationDetailsDTO> locations;

}
