package com.example.demo.dto.planner.trip;

import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class TripResponseDTO {
    private Long tripId;
    private String tripName;
    private String tripArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long budget;
    private List<String> participantIds;

    public static TripResponseDTO fromEntity(Trip trip) {
        TripResponseDTO dto = new TripResponseDTO();
        dto.setTripId(trip.getTripId());
        dto.setTripName(trip.getTripName());
        dto.setTripArea(trip.getTripArea());
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        dto.setBudget(trip.getBudget());
        dto.setParticipantIds(trip.getParticipants().stream()
                .map(User::getUserId)
                .map(String::valueOf) // Long을 다시 String으로 변환
                .collect(Collectors.toList()));
        return dto;
    }
}
