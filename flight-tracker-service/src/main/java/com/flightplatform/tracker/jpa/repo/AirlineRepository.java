package com.flightplatform.tracker.jpa.repo;

import com.flightplatform.tracker.jpa.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
}