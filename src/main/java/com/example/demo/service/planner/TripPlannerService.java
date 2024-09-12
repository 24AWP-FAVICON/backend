package com.example.demo.service.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.InvalidUserException;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 이 서비스 클래스는 여행 계획과 관련된 주요 비즈니스 로직을 처리합니다.
 * 여행 계획의 생성, 조회, 수정, 삭제 및 공유 기능을 제공합니다.
 * @author minjeong
 * @see TripRepository 여행 계획을 저장 및 조회하기 위한 리포지토리
 * @see UserRepository 사용자의 정보를 조회하기 위한 리포지토리
 * @see TripRequestDTO 여행 계획 생성 요청 데이터를 담고 있는 DTO
 */
@Service
@RequiredArgsConstructor
public class TripPlannerService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 모든 여행 계획을 조회합니다.
     * 데이터베이스에서 모든 여행 계획을 가져와 반환합니다.
     *
     * @return 모든 여행 계획 목록
     */
    @Transactional(readOnly = true)
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    /**
     * 새로운 여행 계획을 생성합니다.
     * 주어진 여행 정보와 참여자 ID 목록을 사용하여 새로운 여행 계획을 생성합니다.
     *
     * @param tripDTO 여행 계획 정보가 담긴 DTO
     * @return 생성된 여행 계획 객체
     */
    @Transactional
    public Trip createTrip(TripRequestDTO tripDTO) {
        // 1. 참여자 확인
        List<User> participants = userRepository.findAllById(tripDTO.getParticipantIds());

        // 2. Trip 객체 생성
        Trip newTrip = new Trip();
        newTrip.setTripName(tripDTO.getTripName());
        newTrip.setTripArea(tripDTO.getTripArea());
        newTrip.setStartDate(tripDTO.getStartDate());
        newTrip.setEndDate(tripDTO.getEndDate());
        newTrip.setBudget(tripDTO.getBudget());
        newTrip.setParticipants(participants);

        // 3. 저장
        return tripRepository.save(newTrip);
    }

    /**
     * 특정 ID의 여행 계획을 조회합니다.
     * 주어진 ID를 사용하여 여행 계획을 조회하며, 해당 ID가 없으면 예외를 발생시킵니다.
     *
     * @param tripId 조회할 여행 계획의 ID
     * @return 조회된 여행 계획 객체
     * @throws TripNotFoundException 여행 계획을 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));
    }

    /**
     * 기존 여행 계획을 수정합니다.
     * 주어진 여행 계획 정보를 사용하여 해당 ID의 여행 계획을 업데이트합니다.
     *
     * @param tripId 수정할 여행 계획의 ID
     * @param trip   수정할 여행 계획 객체
     * @return 수정된 여행 계획 객체
     * @throws TripNotFoundException 여행 계획을 찾을 수 없는 경우 발생
     *
     */
    @Transactional
    public Trip updateTrip(Long tripId, Trip trip) throws TripNotFoundException {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip updateTrip = tripOptional.get();

            // 참여자 ID 리스트를 통해 User 객체들을 조회
            List<User> participants = userRepository.findAllById(trip.getParticipants().stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList()));

            // Trip 객체의 모든 필드 업데이트
            updateTrip.setTripName(trip.getTripName());
            updateTrip.setParticipants(participants);
            updateTrip.setStartDate(trip.getStartDate());
            updateTrip.setEndDate(trip.getEndDate());
            updateTrip.setTripArea(trip.getTripArea());
            updateTrip.setBudget(trip.getBudget());

            // 데이터베이스에 저장
            return tripRepository.save(updateTrip);
        } else {
            throw new TripNotFoundException("Trip with ID " + tripId + " not found");
        }
    }

    /**
     * 특정 여행 계획의 일부를 수정합니다.
     * 주어진 여행 계획 ID와 일부 수정할 정보를 사용하여 계획을 부분적으로 업데이트합니다.
     *
     * @param tripId         수정할 여행 계획의 ID
     * @param tripRequestDTO 부분 수정할 여행 계획 정보가 담긴 DTO
     * @return 수정된 여행 계획 객체
     */
    @Transactional
    public Trip partialUpdateTrip(Long tripId, TripRequestDTO tripRequestDTO) {
        Trip updateTrip = getTripById(tripId);

        if (tripRequestDTO.getParticipantIds() != null) {
            List<User> participants = userRepository.findAllById(tripRequestDTO.getParticipantIds());
            updateTrip.setParticipants(participants);
        }
        if (tripRequestDTO.getTripName() != null) updateTrip.setTripName(tripRequestDTO.getTripName());
        if (tripRequestDTO.getStartDate() != null) updateTrip.setStartDate(tripRequestDTO.getStartDate());
        if (tripRequestDTO.getEndDate() != null) updateTrip.setEndDate(tripRequestDTO.getEndDate());
        if (tripRequestDTO.getTripArea() != null) updateTrip.setTripArea(tripRequestDTO.getTripArea());
        if (tripRequestDTO.getBudget() != null) updateTrip.setBudget(tripRequestDTO.getBudget());

        return tripRepository.save(updateTrip);
    }

    /**
     * 특정 ID의 여행 계획을 삭제합니다.
     * 주어진 여행 계획 ID를 사용하여 해당 계획을 삭제합니다.
     *
     * @param tripId 삭제할 여행 계획의 ID
     */
    @Transactional
    public void deleteTripById(Long tripId) {
        tripRepository.deleteById(tripId);
    }

    /**
     * 다른 사용자와 여행 계획을 공유합니다.
     * 주어진 여행 계획 ID와 사용자 Google ID 목록을 사용하여 계획을 공유합니다.
     *
     * @param tripId       공유할 여행 계획의 ID
     * @param userGoogleIds 초대할 사용자의 Google ID 목록
     * @throws InvalidUserException 주어진 사용자 ID가 유효하지 않은 경우 예외를 발생
     */
    @Transactional
    public void shareTripPlanWithUser(Long tripId, List<String> userGoogleIds) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip with ID " + tripId + " not found"));

        List<User> participants = userRepository.findAllByUserIdIn(userGoogleIds);
        if (participants.isEmpty()) {
            throw new InvalidUserException("User Google IDs are invalid");
        }

        // 기존 참여자 목록을 가져온다
        List<User> existingParticipants = trip.getParticipants();

        // 이미 참여자 목록에 있는 사용자를 제외한 새로운 사용자만 추가한다
        List<User> newParticipants = participants.stream()
                .filter(user -> !existingParticipants.contains(user))
                .collect(Collectors.toList());

        if (!newParticipants.isEmpty()) {
            existingParticipants.addAll(newParticipants);
            tripRepository.save(trip);
        }
    }
}