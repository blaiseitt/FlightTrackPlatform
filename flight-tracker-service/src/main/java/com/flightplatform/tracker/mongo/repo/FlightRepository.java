package com.flightplatform.tracker.mongo.repo;

import com.flightplatform.tracker.mongo.domain.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String> {

    Optional<Flight> findByCallsign(String callsign);

    List<Flight> findByOriginCountry(String country);

    List<Flight> findByLastSeenAfter(Instant after);

    List<Flight> findByOnGroundFalseAndLastSeenAfter(Instant after);

    @Query("{ 'latitude': { $gte: ?0, $lte: ?1 }, 'longitude': { $gte: ?2, $lte: ?3 } }")
    List<Flight> findByBoundingBox(double minLat, double maxLat, double minLon, double maxLon);

    @Query("{ 'squawk': '7700' }")
    List<Flight> findEmergencyFlights();

    long countByOnGroundFalseAndLastSeenAfter(Instant after);
}
