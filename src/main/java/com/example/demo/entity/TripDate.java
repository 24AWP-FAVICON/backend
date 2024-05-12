package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
@ToString
public class TripDate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tripDateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    private LocalDate tripDate;

    private int tripDay;

    @Column(nullable = true)
    private Long budget;

    // 숙소와 연결
    @OneToOne(mappedBy = "tripDate")
    private Accomodation accomodation;

    // 장소와 연결
    @OneToMany(mappedBy = "tripDate")
    private List<Location> location;
}
