package com.flightplatform.weather.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "airports")
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "icao_code", nullable = false, unique = true)
    private String icaoCode;

    @Column(name = "iata_code")
    private String iataCode;

    private String name;
    private String city;
    private double latitude;
    private double longitude;
}
