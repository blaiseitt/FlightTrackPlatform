package com.flightplatform.tracker.service;

import com.flightplatform.tracker.jpa.entity.Airline;
import com.flightplatform.tracker.jpa.repo.AirlineRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirlineCache {

    private final AirlineRepository airlineRepository;
    private Map<String, Airline> cacheByIcao;
    private final Set<String> loggedUnknowns = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void load() {
        cacheByIcao = airlineRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Airline::getIcaoCode, Function.identity()));

        log.info("Loaded {} airlines into cache", cacheByIcao.size());
    }

    public Optional<Airline> findByCallsign(String callsign) {
        if (callsign == null || callsign.isBlank()) {
            return Optional.empty();
        }

        String trimmed = callsign.trim();
        String icaoPrefix = callsign.trim().substring(0, Math.min(3, callsign.trim().length()));
        Optional<Airline> result = Optional.ofNullable(cacheByIcao.get(icaoPrefix));

        if (result.isEmpty() && loggedUnknowns.add(icaoPrefix)) {
            log.warn("Unknown airline prefix not found in database {}",
                    StructuredArguments.entries(Map.of(
                            "event_type", "UNKNOWN_AIRLINE",
                            "prefix", icaoPrefix,
                            "callsign", trimmed
                    ))
            );
        }

        return result;
    }
}