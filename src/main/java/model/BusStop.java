package model;

import java.util.Objects;

public record BusStop(String name, double stopLat, double stopLon) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BusStop busStop = (BusStop) o;
    return name.equals(busStop.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
