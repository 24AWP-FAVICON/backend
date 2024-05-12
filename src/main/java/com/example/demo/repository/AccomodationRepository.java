package com.example.demo.repository;

import com.example.demo.entity.Accomodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccomodationRepository extends JpaRepository<Accomodation, Long> {

}
