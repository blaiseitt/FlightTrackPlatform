package com.flightplatform.tracker.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        log.info("Creating MongoDB indexes for flights collection...");
        IndexOperations indexOps = mongoTemplate.indexOps("flights");

        indexOps.ensureIndex(new Index().on("callsign", Sort.Direction.ASC).sparse());
        indexOps.ensureIndex(new Index().on("lastSeen", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("originCountry", Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("squawk", Sort.Direction.ASC).sparse());

        indexOps.ensureIndex(
                new Index()
                        .on("onGround", Sort.Direction.ASC)
                        .on("lastSeen", Sort.Direction.DESC)
        );

        log.info("MongoDB indexes created successfully");
    }
}