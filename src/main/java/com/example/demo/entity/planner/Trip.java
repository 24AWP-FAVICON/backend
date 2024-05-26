package com.example.demo.entity.planner;

import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "tripId")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tripId;

    @ManyToMany
    @JoinTable(
            name = "trip_participants",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @Column(length = 50)
    private String tripName;

    @Column(length = 50)
    private String tripArea;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = true)
    private Long budget;

//    private Long roomId;

    // 세부일정 (TripDate) 엔티티와 연결
    @OneToMany(mappedBy = "trip")
    @JsonManagedReference // 순환 참조 방지 (부모 엔티티에서 자식 엔티티를 직렬화)
    private List<TripDate> tripDates;

}
