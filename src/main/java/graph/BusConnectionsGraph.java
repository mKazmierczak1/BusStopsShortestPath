package graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import model.BusStop;
import model.Connection;
import org.javatuples.Pair;

@RequiredArgsConstructor
public class BusConnectionsGraph {

  private final Map<String, BusStop> nodes;
  private final Map<String, Set<Connection>> edges;
  private final Map<String, Set<String>> directConnections;

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

  public Map<BusStop, Pair<BusStop, LocalTime>> findShortestPath(
      BusStop start, BusStop end, LocalTime time) {
    Queue<Pair<BusStop, Long>> frontier =
        new PriorityQueue<>(Comparator.comparingLong(Pair::getValue1));
    var cameFrom = new HashMap<BusStop, Pair<BusStop, LocalTime>>();
    var costSoFar = new HashMap<BusStop, Long>();

    frontier.add(Pair.with(start, 0L));
    cameFrom.put(start, null);
    costSoFar.put(start, 0L);

    while (!frontier.isEmpty()) {
      var current = frontier.poll();

      if (current.getValue0() == end) {
        break;
      }

      for (var next : directConnections.get(current.getValue0().name())) {
        var earliestConnection =
            getEarliestConnection(current.getValue0().name(), next, time).get();
        var newCost = costSoFar.get(current.getValue0()) + earliestConnection.getTotalTime();

        if (!costSoFar.containsKey(nodes.get(next)) || newCost < costSoFar.get(nodes.get(next))) {
          costSoFar.put(nodes.get(next), newCost);
          frontier.add(Pair.with(nodes.get(next), newCost));
          cameFrom.put(
              nodes.get(next), Pair.with(current.getValue0(), earliestConnection.departureTime()));
        }
      }
    }

    return cameFrom;
  }

  public void printPath(
      Map<BusStop, Pair<BusStop, LocalTime>> cameFrom, BusStop start, BusStop end, LocalTime time) {
    var current = Pair.with(end, time);
    var path = new ArrayList<Pair<BusStop, LocalTime>>();
    while (current.getValue0() != start) {
      path.add(current);
      current = cameFrom.get(current);
    }

    path.add(current);
    Collections.reverse(path);
    path.forEach(pair -> System.out.println(pair.getValue0().name() + ": " + pair.getValue1()));
  }

  //  public Collection<String> Dijkstra(BusStop start, BusStop end) {
  //    if (containsNode(start) && containsNode(end)) {
  //      List<BusStop> path = new LinkedList<>();
  //      BusStop v;
  //      Queue<BusStop> Q = new LinkedList<>();
  //      Long[] d = new Long[nodes.size()];
  //      int[] p = new int[nodes.size()];
  //
  //      for (int i = 0; i < nodes.size(); i++) {
  //        d[i] = Long.MAX_VALUE;
  //        p[i] = -1;
  //      }
  //
  //      d[indexOfNode(start)] = 0L;
  //      Q.add(nodes.get(indexOfNode(start)));
  //
  //      while (!Q.isEmpty()) {
  //        v = Q.poll();
  //
  //        for (Node n : nodes) {
  //          if (indexOfEdge(v.value, n.value) != -1
  //              && d[indexOfNode(n.value)]
  //                  > edges.get(indexOfEdge(v.value, n.value)).weight + d[indexOfNode(v.value)]) {
  //            d[indexOfNode(n.value)] =
  //                edges.get(indexOfEdge(v.value, n.value)).weight + d[indexOfNode(v.value)];
  //            p[indexOfNode(n.value)] = indexOfNode(v.value);
  //          }
  //        }
  //
  //        for (int i = 0; i < nodes.size(); i++) {
  //          if (p[i] == nodes.indexOf(v)) Q.add(nodes.get(i));
  //        }
  //      }
  //
  //      v = nodes.get(indexOfNode(end));
  //      path.add(v.value);
  //
  //      while (v != nodes.get(indexOfNode(start))) {
  //        int pIndex = p[indexOfNode(v.value)];
  //
  //        if (pIndex != -1) path.add(nodes.get(pIndex).value);
  //        else return null;
  //
  //        v = nodes.get(pIndex);
  //      }
  //
  //      Collections.reverse(path);
  //
  //      return path;
  //    }
  //
  //    return null;
  //  }

  private boolean containsNode(BusStop value) {
    return nodes.containsKey(value.name());
  }

  private Optional<Connection> getEarliestConnection(String start, String end, LocalTime time) {
    return getEdges(start, end).stream()
        .map(
            connection ->
                Pair.with(
                    connection,
                    connection.departureTime().isAfter(time)
                        ? Duration.between(time, connection.departureTime()).toMinutes()
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
