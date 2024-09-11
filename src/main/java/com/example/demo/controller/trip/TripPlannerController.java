package com.example.demo.controller.trip;

import com.example.demo.dto.planner.TripCreationDTO;
import com.example.demo.dto.planner.TripPatchDTO;
import com.example.demo.dto.planner.TripResponseDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.repository.planner.AccommodationRepository;
import com.example.demo.repository.planner.LocationRepository;
import com.example.demo.repository.planner.TripDateRepository;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.tripPlanner.TripNotFoundException;
import com.example.demo.service.tripPlanner.TripPlannerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
public class TripPlannerController {

    private final JwtCheckService jwtCheckService;
    private final TripPlannerService tripPlannerService;

    /*
    전체 여행 계획 조회
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

    /*
    전체 여행 계획 생성
     */
    @PostMapping("/trip")
    public ResponseEntity<Trip> addNewTrip(@RequestBody TripCreationDTO tripDTO,
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

    /*
    특정 여행 계획 조회
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

    /*
    특정 여행 계획 수정
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

    /*
    특정 여행 계획 일부 수정
     */
    @PatchMapping("/trip/{tripId}")
    public ResponseEntity<TripResponseDTO> partialUpdateTripById(@PathVariable("tripId") Long tripId,
                                                      @RequestBody TripPatchDTO tripPatchDTO,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        jwtCheckService.checkJwt(request, response);

        try {
            Trip updatedTrip = tripPlannerService.partialUpdateTrip(tripId, tripPatchDTO);
            TripResponseDTO responseDTO = TripResponseDTO.fromEntity(updatedTrip);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (TripNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    특정 여행 계획 삭제
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
}