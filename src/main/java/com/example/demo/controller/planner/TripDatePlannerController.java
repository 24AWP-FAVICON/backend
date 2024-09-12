package com.example.demo.controller.planner;

import com.example.demo.dto.planner.tripDate.TripDateRequestDTO;
import com.example.demo.dto.planner.tripDate.*;
import com.example.demo.entity.planner.TripDate;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.planner.TripDateNotFoundException;
import com.example.demo.service.planner.TripDatePlannerService;
import com.example.demo.service.planner.TripNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/planner")
@RequiredArgsConstructor
@Slf4j
public class TripDatePlannerController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripDateRepository tripDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;
    private final JwtCheckService jwtCheckService;
    private final TripDatePlannerService tripDatePlannerService;

    /*
    특정 여행 계획 내 세부 일정 조회
    */
    @GetMapping("/trip/{tripId}/detail")
    public ResponseEntity<List<TripDate>> getTripDetails(@PathVariable("tripId") Long tripId,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            List<TripDate> tripDates = tripDatePlannerService.getTripDates(tripId);
            return new ResponseEntity<>(tripDates, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 작성
     */
    @PostMapping("/trip/{tripId}/detail")
    @Transactional
    public ResponseEntity<TripDateResponseDTO> addTripDetail(@PathVariable("tripId") Long tripId,
                                                             @RequestBody TripDateRequestDTO tripDateRequestDTO,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate createdTripDate = tripDatePlannerService.addTripDetail(tripId, tripDateRequestDTO);
            TripDateResponseDTO responseDTO = new TripDateResponseDTO();
            responseDTO.setTripDateId(createdTripDate.getTripDateId());
            responseDTO.setTripDate(createdTripDate.getTripDate());
            responseDTO.setTripDay(createdTripDate.getTripDay());
            responseDTO.setBudget(createdTripDate.getBudget());
            responseDTO.setAccommodation(new AccommodationResponseDTO(
                    createdTripDate.getAccommodation().getAccommodationName(),
                    createdTripDate.getAccommodation().getAccommodationLocation()
            ));
            responseDTO.setLocations(createdTripDate.getLocations().stream()
                    .map(loc -> new LocationResponseDTO(loc.getLocationName(), loc.getLocationAddress()))
                    .collect(Collectors.toList()));
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 조회
     */
    @GetMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> getTripDateById(@PathVariable("tripId") Long tripId,
                                                    @PathVariable("tripDateId") Long tripDateId,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate tripDate = tripDatePlannerService.getTripDateById(tripId, tripDateId);
            return new ResponseEntity<>(tripDate, HttpStatus.OK);
        } catch (TripNotFoundException | TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 전체 수정
     */
    @PutMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateCompleteTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                                     @PathVariable("tripDateId") Long tripDateId,
                                                                     @RequestBody TripDateRequestDTO tripDatePatchDTO,
                                                                     HttpServletRequest request,
                                                                     HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateCompleteTripDateDetailById(tripDateId, tripDatePatchDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 일부만 수정
     */
    @PatchMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<TripDate> updateTripDateDetailById(@PathVariable("tripId") Long tripId,
                                                             @PathVariable("tripDateId") Long tripDateId,
                                                             @RequestBody TripDateRequestDTO tripDateRequestDTO,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            TripDate updatedTripDate = tripDatePlannerService.updateTripDateDetailById(tripDateId, tripDateRequestDTO);
            return new ResponseEntity<>(updatedTripDate, HttpStatus.OK);
        } catch (TripDateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 내 세부 일정 삭제
     */
    @DeleteMapping("/trip/{tripId}/detail/{tripDateId}")
    public ResponseEntity<String> deleteTripDateById(@PathVariable("tripId") Long tripId,
                                                     @PathVariable("tripDateId") Long tripDateId,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            tripDatePlannerService.deleteTripDateById(tripDateId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TripDateNotFoundException e) {
            return new ResponseEntity<>("Trip Date not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting Trip Date: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}