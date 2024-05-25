package com.example.demo.entity.planner;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "locationId")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_date_id", nullable = false)
    private TripDate tripDate;

    private String locationName;

    @Column(length = 50)
    private String locationAddress;

    public Location(String name, TripDate newTripDate) {
    }

    public Location(String name, String address, TripDate newTripDate) {
        this.locationName = name;
        this.locationAddress = address;
        this.tripDate = newTripDate;
    }

}
