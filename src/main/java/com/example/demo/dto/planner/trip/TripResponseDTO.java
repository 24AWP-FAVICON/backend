package com.example.demo.dto.planner.trip;

import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 여행 계획에 대한 응답 데이터를 담고 있는 DTO입니다.
 * 서버에서 클라이언트로 여행 계획 정보를 전달할 때 사용됩니다.
 */
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

    /**
     * 주어진 Trip 엔티티를 기반으로 TripResponseDTO를 생성합니다.
     *
     * @param trip Trip 엔티티 객체
     * @return 생성된 TripResponseDTO 객체
     */
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
