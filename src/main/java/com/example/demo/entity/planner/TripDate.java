package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "tripDateId")
public class TripDate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tripDateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonBackReference // 순환 참조 방지
    private Trip trip;

    private LocalDate tripDate;

    private Integer tripDay;

    @Column(nullable = true)
    private Long budget;

    // 숙소와 연결
    @OneToOne(mappedBy = "tripDate")
    private Accommodation accommodation;

    // 장소와 연결
    @OneToMany(mappedBy = "tripDate")
    private List<Location> locations;
}
