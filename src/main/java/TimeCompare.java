import graph.GraphProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;

public class TimeCompare {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var start = graph.getNode("Klimasa");
    var end = graph.getNode("Redycka");
    var time = LocalTime.parse("13:00:00");
    var mode = "t";

    System.out.printf("Path from %s to %s\n", start.name(), end.name());

    var results = new ArrayList<Long>();

    for (int i = 0; i < 100; i++) {
      var currentTime = Instant.now();

      switch (mode) {
        case "t" -> graph.findShortestPathTimeCriteria(start, end, time, 10D);
        case "p" -> graph.findShortestPathBusChangeCriteria(start, end, time, 1D);
        case "d" -> graph.findShortestPathDijkstra(start, end, time);
      }

      var duration = Duration.between(currentTime, Instant.now()).toMillis();
      results.add(duration);
      System.out.println("Duration: " + duration + " ms");
    }

    var avg = 0L;

    for (var result : results) {
      avg += result;
    }

    System.out.println("Average duration: " + avg / 100 + " ms");
  }
}
