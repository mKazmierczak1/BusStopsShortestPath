package graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import model.BusStop;
import model.Connection;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

@RequiredArgsConstructor
public class BusConnectionsGraph {

  private final Map<String, BusStop> nodes;
  private final Map<String, Set<Connection>> edges;
  private final Map<String, Set<String>> directConnections;

  private static final LocalTime END_DAY_TIME = LocalTime.parse("23:59:59");

  public final void addNode(BusStop... values) {
    Arrays.stream(values).forEach(value -> nodes.put(value.name(), value));
  }

  public void addEdge(Connection connection) {
    if (containsNode(connection.startStop()) && containsNode(connection.endStop())) {
      addToSet(
          edges,
          getEdgeKey(connection.startStop().name(), connection.endStop().name()),
          connection);
      addToSet(directConnections, connection.startStop().name(), connection.endStop().name());
    }
  }

  public BusStop getNode(String stopName) {
    return nodes.get(stopName);
  }

  public Map<BusStop, Triplet<BusStop, LocalTime, Double>> findShortestPathTimeCriteria(
      BusStop start, BusStop end, LocalTime time, double heuristicWeight) {
    return findShortestPath(
        start,
        end,
        time,
        this::timeCost,
        (a, b) -> Math.abs(a.stopLat() - b.stopLat()) + Math.abs(a.stopLon() - b.stopLon()),
        heuristicWeight,
        true,
        Criteria.TIME_CRITERIA);
  }

  public Map<BusStop, Triplet<BusStop, LocalTime, Double>> findShortestPathBusChangeCriteria(
      BusStop start, BusStop end, LocalTime time, double heuristicWeight) {
    return findShortestPath(
        start,
        end,
        time,
        this::busChangeCost,
        (a, b) -> Math.abs(a.stopLat() - b.stopLat()) + Math.abs(a.stopLon() - b.stopLon()),
        heuristicWeight,
        false,
        Criteria.BUS_CHANGE_CRITERIA);
  }

  public Map<BusStop, Triplet<BusStop, LocalTime, Double>> findShortestPathDijkstra(
      BusStop start, BusStop end, LocalTime time) {
    return findShortestPath(
        start, end, time, this::timeCost, (a, b) -> 0D, 1D, true, Criteria.TIME_CRITERIA);
  }

  public ArrayList<Triplet<BusStop, LocalTime, Double>> getPath(
      Map<BusStop, Triplet<BusStop, LocalTime, Double>> cameFrom, BusStop start, BusStop end) {
    Triplet<BusStop, LocalTime, Double> current = Triplet.with(end, null, 0D);
    var path = new ArrayList<Triplet<BusStop, LocalTime, Double>>();

    while (!current.getValue0().name().equals(start.name())) {
      path.add(current);
      current = cameFrom.get(current.getValue0());
    }

    path.add(current);
    Collections.reverse(path);

    return path;
  }

  public String getRandomBusStopName() {
    var nodesNames = nodes.keySet().toArray(String[]::new);
    return nodesNames[new Random().nextInt(0, nodesNames.length - 1)];
  }

  private Map<BusStop, Triplet<BusStop, LocalTime, Double>> findShortestPath(
      BusStop start,
      BusStop end,
      LocalTime time,
      BiFunction<Quartet<BusStop, Double, LocalTime, String>, Connection, Double> cost,
      BiFunction<BusStop, BusStop, Double> heuristic,
      double heuristicWeight,
      boolean earlyFinish,
      Criteria criteria) {
    Queue<Quartet<BusStop, Double, LocalTime, String>> frontier =
        new PriorityQueue<>(Comparator.comparingDouble(Quartet::getValue1));
    var cameFrom = new HashMap<BusStop, Triplet<BusStop, LocalTime, Double>>();
    var costSoFar = new HashMap<BusStop, Double>();

    frontier.add(Quartet.with(start, 0D, time, ""));
    cameFrom.put(start, null);
    costSoFar.put(start, 0D);

    while (!frontier.isEmpty()) {
      var current = frontier.poll();
      var currentNode = current.getValue0();

      if (earlyFinish && currentNode == end) {
        break;
      }

      var neighbours = directConnections.get(currentNode.name());

      if (neighbours == null) {
        continue;
      }

      for (var next : neighbours) {
        if (next.equals(currentNode.name())) {
          continue;
        }

        var nextNode = nodes.get(next);
        var connection =
            switch (criteria) {
              case TIME_CRITERIA -> getEarliestConnection(
                      currentNode.name(), next, current.getValue2())
                  .get();
              case BUS_CHANGE_CRITERIA -> getIdenticalLine(
                      currentNode.name(), next, current.getValue3())
                  .orElse(
                      getEarliestConnection(currentNode.name(), next, current.getValue2()).get());
            };
        var connectionArrivalTime = connection.arrivalTime();
        var newCost = costSoFar.get(currentNode) + cost.apply(current, connection);

        if (!costSoFar.containsKey(nextNode) || newCost < costSoFar.get(nextNode)) {
          costSoFar.put(nextNode, newCost);
          frontier.add(
              Quartet.with(
                  nextNode,
                  newCost + heuristic.apply(end, nextNode) * heuristicWeight,
                  connectionArrivalTime,
                  connection.line()));

          if (cameFrom.get(currentNode) == null
              || !cameFrom.get(currentNode).getValue0().name().equals(next)) {
            cameFrom.put(
                nodes.get(next), Triplet.with(currentNode, connectionArrivalTime, newCost));
          }
        }
      }
    }

    return cameFrom;
  }

  private Double timeCost(
      Quartet<BusStop, Double, LocalTime, String> current, Connection connection) {
    var connectionTime =
        Duration.between(current.getValue2(), connection.arrivalTime()).toMinutes();
    return (double)
        (connectionTime > 0
            ? connectionTime
            : connectionTime * -1
                + Duration.between(current.getValue2(), END_DAY_TIME).toMinutes());
  }

  private Double busChangeCost(
      Quartet<BusStop, Double, LocalTime, String> current, Connection connection) {
    return connection.line().equals(current.getValue3()) ? 0D : 1D;
  }

  private boolean containsNode(BusStop value) {
    return nodes.containsKey(value.name());
  }

  private Optional<Connection> getEarliestConnection(String start, String end, LocalTime time) {
    return getEdges(start, end).stream()
        .map(
            connection ->
                Pair.with(
                    connection,
                    connection.arrivalTime().isAfter(time)
                        ? Duration.between(time, connection.arrivalTime()).toMinutes()
                        : Long.MAX_VALUE))
        .min(Comparator.comparingLong(Pair::getValue1))
        .map(Pair::getValue0);
  }

  private Optional<Connection> getIdenticalLine(String start, String end, String line) {
    return getEdges(start, end).stream()
        .filter(connection -> connection.line().equals(line))
        .findAny();
  }

  private Set<Connection> getEdges(String start, String end) {
    return edges.get(getEdgeKey(start, end));
  }

  private String getEdgeKey(String start, String end) {
    return String.join("|", start, end);
  }

  private <V> void addToSet(Map<String, Set<V>> map, String key, V value) {
    map.compute(
        key,
        (k, values) -> {
          if (values == null) {
            values = new HashSet<>();
          }

          values.add(value);
          return values;
        });
  }

  private enum Criteria {
    TIME_CRITERIA,
    BUS_CHANGE_CRITERIA
  }
}
