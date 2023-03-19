package graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import model.BusStop;
import model.Connection;
import org.javatuples.Pair;
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

  public Map<BusStop, Pair<BusStop, LocalTime>> findShortestPathDijkstra(
      BusStop start, BusStop end, LocalTime time) {
    Queue<Triplet<BusStop, Long, LocalTime>> frontier =
        new PriorityQueue<>(Comparator.comparingLong(Triplet::getValue1));
    var cameFrom = new HashMap<BusStop, Pair<BusStop, LocalTime>>();
    var costSoFar = new HashMap<BusStop, Long>();

    frontier.add(Triplet.with(start, 0L, time));
    cameFrom.put(start, null);
    costSoFar.put(start, 0L);

    while (!frontier.isEmpty()) {
      var current = frontier.poll();
      var currentNode = current.getValue0();

      if (currentNode == end) {
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
        var earliestConnection =
            getEarliestConnection(currentNode.name(), next, current.getValue2()).get();
        var connectionArrivalTime = earliestConnection.arrivalTime();
        var connectionTime =
            Duration.between(current.getValue2(), connectionArrivalTime).toMinutes();
        var newCost =
            costSoFar.get(currentNode)
                + (connectionTime > 0
                    ? connectionTime
                    : connectionTime * -1
                        + Duration.between(current.getValue2(), END_DAY_TIME).toMinutes());

        if (!costSoFar.containsKey(nextNode) || newCost < costSoFar.get(nextNode)) {
          costSoFar.put(nextNode, newCost);
          frontier.add(Triplet.with(nextNode, newCost, connectionArrivalTime));

          if (cameFrom.get(currentNode) == null
              || !cameFrom.get(currentNode).getValue0().name().equals(next)) {
            cameFrom.put(nodes.get(next), Pair.with(currentNode, connectionArrivalTime));
          }
        }
      }
    }

    return cameFrom;
  }

  public void printPath(
      Map<BusStop, Pair<BusStop, LocalTime>> cameFrom, BusStop start, BusStop end, LocalTime time) {
    Pair<BusStop, LocalTime> current = Pair.with(end, null);
    var path = new ArrayList<Pair<BusStop, LocalTime>>();

    //    cameFrom.forEach(
    //        (busStop, pair) -> System.out.println("Came from: " + busStop.name() + " Stop: " +
    // pair));

    while (!current.getValue0().name().equals(start.name())) {
      // System.out.println(current);
      path.add(current);
      current = cameFrom.get(current.getValue0());
    }

    path.add(current);
    Collections.reverse(path);
    path.forEach(pair -> System.out.println(pair.getValue0().name() + ": " + pair.getValue1()));
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
}
