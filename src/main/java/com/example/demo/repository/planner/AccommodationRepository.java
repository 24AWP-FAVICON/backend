package com.example.demo.repository.planner;

import com.example.demo.entity.planner.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Optional<Accommodation> findByAccommodationName(String accommodationName);
}
