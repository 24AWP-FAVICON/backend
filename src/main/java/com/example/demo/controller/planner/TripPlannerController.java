package com.example.demo.controller.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.dto.planner.trip.TripResponseDTO;
import com.example.demo.dto.users.user.UserIdsDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.exception.InvalidUserException;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.planner.TripDateNotFoundException;
import com.example.demo.service.planner.TripNotFoundException;
import com.example.demo.service.planner.TripPlannerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TripPlannerController | 이 컨트롤러는 여행 계획과 관련된 요청을 처리합니다.
 * 사용자가 여행 계획을 생성, 조회, 수정, 삭제, 공유할 수 있도록 합니다.
 * @see TripPlannerService 여행 계획과 관련된 비즈니스 로직을 처리하는 서비스 클래스
 * @see TripRequestDTO 여행 계획 생성 요청 데이터를 담고 있는 DTO
 * @see TripResponseDTO 여행 계획 응답 데이터를 담고 있는 DTO
 * @see JwtCheckService JWT 토큰을 검증하여 요청의 유효성을 검사하는 서비스
 * @see TripNotFoundException 여행 계획이 존재하지 않을 때 발생하는 예외
 * @author minjeong
 */
@RestController()
@RequestMapping("/planner")
@RequiredArgsConstructor
@Slf4j
public class TripPlannerController {

    private final JwtCheckService jwtCheckService;
    private final TripPlannerService tripPlannerService;

    /**
     * 모든 여행 계획을 조회합니다.
     * 이 메서드는 JWT 토큰을 확인한 후 모든 여행 계획을 가져옵니다.
     * 만약 여행 계획이 없다면, `NO_CONTENT` 상태를 반환합니다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 여행 계획 리스트와 상태 코드를 포함한 응답 엔티티
     */
    @GetMapping("/trip")
    public ResponseEntity<List<TripResponseDTO>> getAllTrips(HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);
        try {
            List<Trip> trips = tripPlannerService.getAllTrips();
            List<TripResponseDTO> responseDTOs = trips.stream()
                    .map(TripResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            if (trips.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 새로운 여행 계획을 생성합니다.
     * 사용자가 제공한 여행 계획 정보를 기반으로 새로운 계획을 생성하고 저장합니다.
     *
     * @param tripDTO  생성할 여행 계획 정보
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 생성된 여행 계획과 상태 코드를 포함한 응답 엔티티
     */
    @PostMapping("/trip")
    public ResponseEntity<Trip> addNewTrip(@RequestBody TripRequestDTO tripDTO,
                                           HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            Trip createdTrip = tripPlannerService.createTrip(tripDTO);
            return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획을 조회합니다.
     * 주어진 여행 ID를 사용하여 해당 계획을 조회합니다.
     *
     * @param tripId   조회할 여행 계획의 ID
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 조회된 여행 계획과 상태 코드를 포함한 응답 엔티티
     * @throws TripNotFoundException    여행 계획이 없는 경우 404 예외를 발생
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable("tripId") Long tripId,
                                            HttpServletRequest request, HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            Trip trip = tripPlannerService.getTripById(tripId);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(trip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획을 수정합니다.
     * 주어진 여행 ID와 수정할 정보를 사용하여 여행 계획을 업데이트합니다.
     *
     * @param tripId   수정할 여행 계획의 ID
     * @param trip     수정할 여행 계획 정보
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 수정된 여행 계획과 상태 코드를 포함한 응답 엔티티
     * @throws TripNotFoundException    여행 계획이 없는 경우 404 예외를 발생
     */
    @PutMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> updateTripById(@PathVariable("tripId") Long tripId,
                                               @RequestBody Trip trip,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Trip updatedTrip = tripPlannerService.updateTrip(tripId, trip);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(updatedTrip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획의 일부를 수정합니다.
     * 주어진 여행 ID와 부분적으로 수정할 정보를 사용하여 여행 계획을 업데이트합니다.
     *
     * @param tripId          수정할 여행 계획의 ID
     * @param tripRequestDTO  부분 수정할 여행 계획 정보
     * @param request         HTTP 요청 객체
     * @param response        HTTP 응답 객체
     * @return 수정된 여행 계획과 상태 코드를 포함한 응답 엔티티
     * @throws TripNotFoundException    여행 계획이 없는 경우 404 예외를 발생
     */
    @PatchMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> partialUpdateTripById(@PathVariable("tripId") Long tripId,
                                                      @RequestBody TripRequestDTO tripRequestDTO,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Trip updatedTrip = tripPlannerService.partialUpdateTrip(tripId, tripRequestDTO);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(updatedTrip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 여행 계획을 삭제합니다.
     * 주어진 여행 ID에 해당하는 계획을 삭제합니다.
     *
     * @param tripId   삭제할 여행 계획의 ID
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 상태 코드를 포함한 응답 엔티티
     * @throws TripNotFoundException    여행 계획이 없는 경우 404 예외를 발생
     */
    @DeleteMapping("/trip/{tripId}")
    public ResponseEntity<String> deleteTripById(@PathVariable("tripId") Long tripId,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        try {
            tripPlannerService.deleteTripById(tripId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 다른 사용자와 여행 계획을 공유하고 초대합니다.
     * <p>주어진 여행 ID와 사용자 Google ID 목록을 사용하여 계획을 공유합니다.</p>
     *
     * @param tripId        공유할 여행 계획의 ID
     * @param userIdsDTO    초대할 사용자의 Google ID 목록
     * @param request       HTTP 요청 객체
     * @param response      HTTP 응답 객체
     * @return 상태 코드를 포함한 응답 엔티티
     * @throws TripNotFoundException    여행 계획이 없는 경우 404 예외를 발생
     * @throws InvalidUserException 주어진 사용자 ID가 유효하지 않은 경우 400 예외를 발생
     */
    @PostMapping("/trip/{tripId}/share")
    public ResponseEntity<String> shareTripPlanWithUser(@PathVariable("tripId") Long tripId,
                                                        @RequestBody UserIdsDTO userIdsDTO,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            tripPlannerService.shareTripPlanWithUser(tripId, userIdsDTO.getUserGoogleIds());
            return new ResponseEntity<>("User added to the trip successfully", HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>("Trip not found", HttpStatus.NOT_FOUND);
        } catch (InvalidUserException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error sharing trip plan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}