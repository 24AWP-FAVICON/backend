package com.example.demo.controller.planner;

import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.dto.planner.tripDate.*;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.service.planner.ComponentNotFoundException;
import com.example.demo.service.planner.TripDatePlannerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TripDatePlannerController | 이 컨트롤러는 여행 계획의 세부 일정과 관련된 요청을 처리합니다.
 * 사용자가 특정 여행 계획 내에서 일정을 조회, 작성, 수정, 삭제할 수 있도록 합니다.
 * @author minjeong
 * @see TripDatePlannerService 여행 계획의 세부 일정을 관리하는 서비스로, 이 컨트롤러의 요청을 처리
 * @see TripDateRequestDTO 세부 일정 작성 및 수정 요청에 사용되는 DTO
 * @see TripDate 여행 계획 내의 세부 일정을 나타내는 엔티티 클래스
 * @see AccommodationResponseDTO 숙소 정보를 응답하기 위한 DTO
 * @see LocationResponseDTO 위치(장소) 정보를 응답하기 위한 DTO
 * @see ComponentNotFoundException 여행 계획 또는 세부 일정이 존재하지 않을 때 발생하는 예외
 */
@RestController()
@RequestMapping("/planner")
@RequiredArgsConstructor
@Slf4j
public class TripDatePlannerController {

    private final TripDatePlannerService tripDatePlannerService;

    /**
     * 특정 여행 계획 내의 모든 세부 일정을 조회합니다.
     * 주어진 여행 ID를 사용하여 해당 여행의 모든 일정을 가져옵니다.
     *
     * @param tripId   조회할 여행 계획의 ID
     * @return 여행 계획의 세부 일정 목록과 상태 코드를 포함한 응답 엔티티
     * @throws ComponentNotFoundException    여행 일정이 없는 경우 404 예외를 발생
     */
    @GetMapping("/trip/{tripId}/detail")
    public ResponseEntity<List<TripDate>> getTripDetails(@PathVariable("tripId") Long tripId) {
        try {
            List<TripDate> tripDates = tripDatePlannerService.getTripDates(tripId);
            return new ResponseEntity<>(tripDates, HttpStatus.OK);
        } catch (ComponentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획 내에서 새로운 세부 일정을 작성합니다.
     * 주어진 여행 ID와 일정 정보를 사용하여 새로운 일정을 생성합니다.
     *
     * @param tripId            작성할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateRequestDTOs 생성할 세부 일정 정보 리스트가 담긴 DTO
     * @return 생성된 세부 일정과 상태 코드를 포함한 응답 엔티티
     */
    @PostMapping("/trip/{tripId}/detail")
    @Transactional
    public ResponseEntity<List<TripDateResponseDTO>> addMultipleTripDetails(@PathVariable("tripId") Long tripId,
                                                                            @RequestBody List<TripDateRequestDTO> tripDateRequestDTOs) {
        try {
            List<TripDateResponseDTO> responseDTOs = tripDateRequestDTOs.stream().map(tripDateRequestDTO -> {
                TripDate createdTripDate = tripDatePlannerService.addTripDetail(tripId, tripDateRequestDTO);

                TripDateResponseDTO responseDTO = new TripDateResponseDTO();
                responseDTO.setTripDateId(createdTripDate.getTripDateId());
                responseDTO.setTripDate(createdTripDate.getTripDate());
                responseDTO.setTripDay(createdTripDate.getTripDay());
                responseDTO.setBudget(createdTripDate.getBudget());

                if (createdTripDate.getAccommodation() != null) {
                    responseDTO.setAccommodation(new AccommodationResponseDTO(
                            createdTripDate.getAccommodation().getAccommodationName(),
                            createdTripDate.getAccommodation().getAccommodationLocation()
                    ));
                } else {
                    responseDTO.setAccommodation(null);  // 숙소가 없을 경우 null로 설정
                }

                responseDTO.setLocations(createdTripDate.getLocations().stream()
                        .map(loc -> new LocationResponseDTO(loc.getLocationName(), loc.getLocationAddress()))
                        .collect(Collectors.toList()));

                return responseDTO;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(responseDTOs, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획 내의 특정 세부 일정을 조회합니다.
     * 주어진 여행 ID와 세부 일정 ID를 사용하여 일정을 조회합니다.
     *
     * @param tripId      조회할 여행 계획의 ID
     * @param tripDateId  조회할 세부 일정의 ID
     * @return 조회된 세부 일정과 상태 코드를 포함한 응답 엔티티
     * @throws ComponentNotFoundException    여행 내 세부 일정이 없는 경우 404 예외를 발생
     */
    @GetMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId,
                                                    @PathVariable("tripDateId") Long tripDateId) {
        try {
            TripDate tripDate = tripDatePlannerService.getTripDateById(tripId, tripDateId);
            return new ResponseEntity<>(tripDate, HttpStatus.OK);
        } catch (ComponentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획 내의 특정 세부 일정을 전체 수정합니다.
     * 주어진 여행 ID와 세부 일정 ID, 그리고 수정할 정보를 사용하여 일정을 업데이트합니다.
     *
     * @param tripId            수정할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateId        수정할 세부 일정의 ID
     * @param tripDatePatchDTO  수정할 세부 일정 정보가 담긴 DTO
     * @return 수정된 세부 일정과 상태 코드를 포함한 응답 엔티티
     * @throws ComponentNotFoundException    여행 내 세부 일정이 없는 경우 404 예외를 발생
     */
    @PutMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateCompleteTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                                     @PathVariable("tripDateId") Long tripDateId,
                                                                     @RequestBody TripDateRequestDTO tripDatePatchDTO) {

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateCompleteTripDateDetailById(tripDateId, tripDatePatchDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (ComponentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획 내의 특정 세부 일정을 부분적으로 수정합니다.
     * 주어진 여행 ID와 세부 일정 ID, 그리고 수정할 정보를 사용하여 일정을 부분적으로 업데이트합니다.
     *
     * @param tripId            수정할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateId        수정할 세부 일정의 ID
     * @param tripDateRequestDTO 부분 수정할 세부 일정 정보가 담긴 DTO
     * @return 수정된 세부 일정과 상태 코드를 포함한 응답 엔티티
     * @throws ComponentNotFoundException    여행 내 세부 일정이 없는 경우 404 예외를 발생
     */
    @PatchMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                             @PathVariable("tripDateId") Long tripDateId,
                                                             @RequestBody TripDateRequestDTO tripDateRequestDTO) {

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateTripDateDetailById(tripDateId, tripDateRequestDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (ComponentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획 내의 특정 세부 일정을 삭제합니다.
     * 주어진 여행 ID와 세부 일정 ID를 사용하여 일정을 삭제합니다.
     *
     * @param tripId      삭제할 세부 일정이 속한 여행 계획의 ID
     * @param tripDateId  삭제할 세부 일정의 ID
     * @return 상태 코드를 포함한 응답 엔티티
     * @throws ComponentNotFoundException    여행 내 세부 일정이 없는 경우 404 예외를 발생
     */
    @DeleteMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<String> deleteTripDateById(@PathVariable("tripId") Long tripId,
                                                     @PathVariable("tripDateId") Long tripDateId) {

        try {
            tripDatePlannerService.deleteTripDateById(tripDateId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ComponentNotFoundException e) {
            return new ResponseEntity<>("Trip Date not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting Trip Date: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}