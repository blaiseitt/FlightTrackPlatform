package com.flightplatform.weather.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        try {
            log.info("Creating MongoDB indexes for weather_snapshots collection...");

            IndexOperations indexOps = mongoTemplate.indexOps("weather_snapshots");

            indexOps.ensureIndex(new CompoundIndexDefinition(
                    new Document("bboxId", 1).append("fetchedAt", -1)));

            indexOps.ensureIndex(new CompoundIndexDefinition(
                    new Document("airportIcao", 1).append("fetchedAt", -1)));

            indexOps.ensureIndex(new Index()
                    .on("isAirport", Sort.Direction.ASC)
                    .on("fetchedAt", Sort.Direction.DESC));

            log.info("MongoDB indexes for weather_snapshots created successfully");
        } catch (Exception e) {
            log.warn("Could not create MongoDB indexes on startup: {}", e.getMessage());
        }
    }
}
