package com.flightplatform.weather.mongo.repo;

import com.flightplatform.weather.mongo.domain.WeatherSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherSnapshotRepository extends MongoRepository<WeatherSnapshot, String> {
}
