package com.flightplatform.weather.jpa.repo;

import com.flightplatform.weather.jpa.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
}
