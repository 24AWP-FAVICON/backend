package com.example.demo.service.planner;

import com.example.demo.dto.planner.trip.TripRequestDTO;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.Role;
import com.example.demo.entity.users.user.User;
import com.example.demo.repository.planner.TripRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.exception.InvalidUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripPlannerService tripPlannerService;

    private Trip trip;
    private TripRequestDTO tripRequestDTO;
    private List<User> participants;

    @BeforeEach
    void setUp() {
        try (AutoCloseable closeable = MockitoAnnotations.openMocks(this)) {
            // User 객체 생성
            User user1 = new User();
            user1.setUserId("minbory925@gmail.com");
            user1.setNickname("Minjeong");
            user1.setRole(Role.ROLE_USER);
            user1.setRecentConnect(LocalDateTime.now());
            user1.setCreatedAt(LocalDate.now());

            User user2 = new User();
            user2.setUserId("deepdevming@gmail.com");
            user2.setNickname("Mingguriguri");
            user2.setRole(Role.ROLE_USER);
            user2.setRecentConnect(LocalDateTime.now());
            user2.setCreatedAt(LocalDate.now());

            participants = Arrays.asList(user1, user2);

            // Trip 엔티티 객체 초기화 (수정 가능한 리스트로 변경)
            trip = new Trip();
            trip.setTripId(1L);
            trip.setTripName("Summer Trip");
            trip.setTripArea("Seoul");
            trip.setStartDate(LocalDate.now());
            trip.setEndDate(LocalDate.now().plusDays(5));
            trip.setBudget(100000L);
            trip.setParticipants(new ArrayList<>()); // 수정 가능한 ArrayList로 설정

            // TripRequestDTO 객체 초기화
            tripRequestDTO = new TripRequestDTO();
            tripRequestDTO.setTripName("Summer Trip");
            tripRequestDTO.setTripArea("Seoul");
            tripRequestDTO.setStartDate(LocalDate.now());
            tripRequestDTO.setEndDate(LocalDate.now().plusDays(5));
            tripRequestDTO.setBudget(100000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("createTrip 성공 - 새로운 여행 계획을 생성할 수 있다")
    void createTrip_success() {
        when(userRepository.findAllById(tripRequestDTO.getParticipantIds())).thenReturn(participants);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip createdTrip = tripPlannerService.createTrip(tripRequestDTO);

        assertNotNull(createdTrip);
        assertEquals(tripRequestDTO.getTripName(), createdTrip.getTripName());
        assertEquals(tripRequestDTO.getTripArea(), createdTrip.getTripArea());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("getTripById 성공 - 특정 ID의 여행 계획을 조회할 수 있다")
    void getTripById_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        Trip foundTrip = tripPlannerService.getTripById(1L);

        assertNotNull(foundTrip);
        assertEquals(1L, foundTrip.getTripId());
        assertEquals("Summer Trip", foundTrip.getTripName());
        verify(tripRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateTrip 성공 - 여행 계획을 수정할 수 있다")
    void updateTrip_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findAllById(any())).thenReturn(participants);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip updatedTrip = tripPlannerService.updateTrip(1L, trip);

        assertNotNull(updatedTrip);
        assertEquals(trip.getTripName(), updatedTrip.getTripName());
        assertEquals(trip.getTripArea(), updatedTrip.getTripArea());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("partialUpdateTrip 성공 - 부분적으로 여행 계획을 수정할 수 있다")
    void partialUpdateTrip_success() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));
        when(userRepository.findAllById(anyList())).thenReturn(participants);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        Trip updatedTrip = tripPlannerService.partialUpdateTrip(1L, tripRequestDTO);

        assertNotNull(updatedTrip);
        assertEquals(tripRequestDTO.getTripName(), updatedTrip.getTripName());
        assertEquals(tripRequestDTO.getTripArea(), updatedTrip.getTripArea());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("partialUpdateTrip 실패 - 여행 계획을 찾을 수 없을 때 ComponentNotFoundException 발생")
    void partialUpdateTrip_notFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 여행 계획이 없을 때 ComponentNotFoundException이 발생하는지 확인
        assertThrows(ComponentNotFoundException.class, () -> {
            tripPlannerService.partialUpdateTrip(1L, tripRequestDTO);
        });

        verify(tripRepository, times(0)).save(any(Trip.class));
    }

    @Test
    @DisplayName("getTripById 실패 - 여행 계획을 찾을 수 없을 때 ComponentNotFoundException 발생")
    void getTripById_notFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 여행 계획이 없을 때 ComponentNotFoundException이 발생하는지 확인
        assertThrows(ComponentNotFoundException.class, () -> {
            tripPlannerService.getTripById(1L);
        });

        verify(tripRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateTrip 실패 - 존재하지 않는 여행 계획을 수정하려 할 때 ComponentNotFoundException 발생")
    void updateTrip_notFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 여행 계획이 없을 때 ComponentNotFoundException이 발생하는지 확인
        assertThrows(ComponentNotFoundException.class, () -> {
            tripPlannerService.updateTrip(1L, trip);
        });

        verify(tripRepository, times(0)).save(any(Trip.class));
    }

    @Test
    @DisplayName("deleteTripById 성공 - 여행 계획을 삭제할 수 있다")
    void deleteTripById_success() {
        doNothing().when(tripRepository).deleteById(1L);

        tripPlannerService.deleteTripById(1L);

        verify(tripRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("getAllTrips 성공 - 모든 여행 계획을 조회할 수 있다")
    void getAllTrips_success() {
        // 여행 계획이 있는 경우
        when(tripRepository.findAll()).thenReturn(Collections.singletonList(trip));

        List<Trip> trips = tripPlannerService.getAllTrips();

        assertNotNull(trips);
        assertEquals(1, trips.size());
        assertEquals(trip.getTripName(), trips.get(0).getTripName());
        verify(tripRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("shareTripPlan 성공 - 여행 계획을 다른 사용자와 공유할 수 있다")
    void shareTripPlan_success() {

        // TripRepository에서 해당 여행 계획을 찾을 수 있도록 설정
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // UserRepository에서 공유할 참여자를 찾을 수 있도록 설정
        when(userRepository.findAllByUserIdIn(Arrays.asList("minbory925@gmail.com", "deepdevming@gmail.com")))
                .thenReturn(participants);

        // 테스트할 메서드 호출
        tripPlannerService.shareTripPlanWithUser(1L, Arrays.asList("minbory925@gmail.com", "deepdevming@gmail.com"));

        // 새 참여자가 추가되었는지 확인
        List<User> addedParticipants = trip.getParticipants();
        assertNotNull(addedParticipants);
        assertEquals(2, addedParticipants.size()); // 참여자 수 확인

        // 참여자의 ID가 예상대로 추가되었는지 확인
        List<String> participantIds = addedParticipants.stream().map(User::getUserId).toList();
        assertTrue(participantIds.containsAll(Arrays.asList("minbory925@gmail.com", "deepdevming@gmail.com")));

        // TripRepository의 save 메서드가 호출되었는지 확인
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("shareTripPlan 실패 - 유효하지 않은 사용자로 인해 여행 계획을 공유할 수 없다")
    void shareTripPlan_invalidUser() {
        // 여행 계획에 참여자가 비어있는 상태로 설정
        trip.setParticipants(Collections.emptyList());

        // TripRepository에서 여행 계획을 찾을 수 있도록 설정
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // UserRepository에서 유효한 사용자가 없는 상황을 설정
        when(userRepository.findAllByUserIdIn(anyList())).thenReturn(Collections.emptyList());

        // InvalidUserException이 발생하는지 확인
        assertThrows(InvalidUserException.class, () -> {
            tripPlannerService.shareTripPlanWithUser(1L, Arrays.asList("invalidUser@gmail.com"));
        });

        // TripRepository의 save 메서드가 호출되지 않았는지 확인
        verify(tripRepository, times(0)).save(any(Trip.class));
    }
}
