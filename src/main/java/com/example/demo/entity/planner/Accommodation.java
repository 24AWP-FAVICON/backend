package com.example.demo.entity.planner;

import com.example.demo.entity.planner.TripDate;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "accommodationId")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accommodationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_date_id")
    private TripDate tripDate;

    private String accommodationName;

    @Column(length = 100)
    private String accommodationLocation;

    // 생성자 추가
    public Accommodation(String accommodationName, String accommodationLocation) {
        this.accommodationName = accommodationName;
        this.accommodationLocation = accommodationLocation;
    }
}