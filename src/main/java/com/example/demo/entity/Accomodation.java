package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString
public class Accomodation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accomodationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_date_id", nullable = false)
    private TripDate tripDateId;

    private String accomodationName;

    @Column(length = 50)
    private String accomodationLocation;
}
