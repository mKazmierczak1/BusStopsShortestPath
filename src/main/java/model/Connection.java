package model;

import java.time.LocalTime;

public record Connection(
    String line,
    LocalTime departureTime,
    LocalTime arrivalTime,
    BusStop startStop,
    BusStop endStop) {}
