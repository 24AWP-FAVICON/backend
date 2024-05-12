package com.example.demo.repository;

import com.example.demo.entity.TripDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TripDateRepository extends JpaRepository<TripDate, Long> {

    List<TripDate> findByTrip_TripId(Long tripDateId);
}
