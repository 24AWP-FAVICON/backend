package com.example.demo.repository.planner;

import com.example.demo.entity.planner.TripDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TripDateRepository extends JpaRepository<TripDate, Long> {

    List<TripDate> findByTrip_TripId(Long tripDateId);
}
