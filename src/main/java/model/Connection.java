package model;

import java.time.Duration;
import java.time.LocalTime;

public record Connection(
    String line,
    LocalTime departureTime,
    LocalTime arrivalTime,
    BusStop startStop,
    BusStop endStop) {

  public long getTotalTime() {
    return Duration.between(arrivalTime, departureTime).toMinutes();
  }
}
