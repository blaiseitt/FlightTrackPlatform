package com.flightplatform.weather.openweather;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherSnapshotRepository extends MongoRepository<WeatherSnapshot, String> {
}
